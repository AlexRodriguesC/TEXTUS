package ch.supsi.documentapplication.configuration;

import ch.supsi.documentapplication.exception.PromptTemplateLoadException;
import ch.supsi.documentapplication.properties.PromptProperties;
import ch.supsi.documentapplication.service.MindMapValidator;
import ch.supsi.documentapplication.service.clients.*;
import ch.supsi.documentapplication.service.clients.OpenAIMindMapClient;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.retry.*;
import org.springframework.util.FileCopyUtils;

@Configuration
@Log4j2
public class OpenAIClientsConfiguration {

    @Bean
    public StTemplateRenderer stTemplateRenderer() {
        return StTemplateRenderer.builder()
                .startDelimiterToken('<')
                .endDelimiterToken('>')
                .build();
    }

    @Bean
    public OpenAIMindMapClient mindMapClient(
            ChatClient.Builder chatBuilder,
            RetryTemplate retryTemplate,
            StTemplateRenderer templateRenderer,
            PromptProperties promptProperties,
            ResourceLoader resourceLoader) {
        var userPromptResource = resourceLoader.getResource(promptProperties.getMindMapExtractionUser());
        String userPromptText = copyToString(userPromptResource);

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(templateRenderer)
                .template(userPromptText)
                .build();

        var systemPromptResource = resourceLoader.getResource(promptProperties.getMindMapExtractionSystem());
        ChatClient chatClient = chatBuilder.defaultSystem(systemPromptResource).build();

        var mindMapValidator = new MindMapValidator();

        return new OpenAIMindMapClient(chatClient, retryTemplate, mindMapValidator, promptTemplate);
    }

    @Bean
    public OpenAISummarizationClient openAISummarizationClient(
            ChatClient.Builder chatBuilder,
            RetryTemplate retryTemplate,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader) {
        var userPromptResource = resourceLoader.getResource(promptProperties.getSummarizationUser());
        String userPromptText = copyToString(userPromptResource);

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(templateRenderer)
                .template(userPromptText)
                .build();

        var systemPromptResource = resourceLoader.getResource(promptProperties.getSummarizationSystem());
        ChatClient chatClient = chatBuilder.defaultSystem(systemPromptResource).build();

        return new OpenAISummarizationClient(chatClient, retryTemplate, promptTemplate);
    }

    @Bean
    public OpenAITopicExtractionClient openAITopicExtractionClient(
            ChatClient.Builder chatBuilder,
            RetryTemplate retryTemplate,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader) {
        var userPromptResource = resourceLoader.getResource(promptProperties.getTopicExtractionUser());
        String userPromptText = copyToString(userPromptResource);

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(templateRenderer)
                .template(userPromptText)
                .build();

        var systemPromptResource = resourceLoader.getResource(promptProperties.getTopicExtractionSystem());
        ChatClient chatClient = chatBuilder.defaultSystem(systemPromptResource).build();

        return new OpenAITopicExtractionClient(chatClient, retryTemplate, promptTemplate);
    }

    private String copyToString(Resource resource) {
        try (var in = resource.getInputStream();
                var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {

            return FileCopyUtils.copyToString(reader);

        } catch (IOException e) {
            log.error(
                    "Failed to load prompt template. This is most likely a programmer error, or the resource {} is missing on the server.",
                    resource.getFilename(),
                    e);
            throw new PromptTemplateLoadException("Failed to load prompt template: " + resource.getDescription(), e);
        }
    }
}
