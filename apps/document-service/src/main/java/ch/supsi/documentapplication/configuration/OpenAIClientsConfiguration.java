package ch.supsi.documentapplication.configuration;

import ch.supsi.documentapplication.exception.PromptTemplateLoadException;
import ch.supsi.documentapplication.properties.AppProperties;
import ch.supsi.documentapplication.properties.PromptProperties;
import ch.supsi.documentapplication.service.MindMapValidator;
import ch.supsi.documentapplication.service.clients.*;
import ch.supsi.documentapplication.service.clients.mindmap.OpenAIMindMapClient;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

@Configuration
public class OpenAIClientsConfiguration {

    @Bean
    public StTemplateRenderer stTemplateRenderer() {
        return StTemplateRenderer.builder()
                .startDelimiterToken('<')
                .endDelimiterToken('>')
                .build();
    }

    @Bean
    public FailureTracker failureTracker(AppProperties appProperties) {
        return LLMFailureTracker.create(appProperties.getMaxLLMMindMapFailures());
    }

    @Bean
    public OpenAIMindMapClient mindMapClient(
            ChatClient.Builder chatBuilder,
            StTemplateRenderer templateRenderer,
            PromptProperties promptProperties,
            ResourceLoader resourceLoader,
            FailureTracker failureTracker) {
        var userPromptResource = resourceLoader.getResource(promptProperties.getMindMapExtractionUser());
        String userPromptText = copyToString(userPromptResource);

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(templateRenderer)
                .template(userPromptText)
                .build();

        var systemPromptResource = resourceLoader.getResource(promptProperties.getMindMapExtractionSystem());
        ChatClient chatClient = chatBuilder.defaultSystem(systemPromptResource).build();

        var mindMapValidator = new MindMapValidator();

        return new OpenAIMindMapClient(chatClient, failureTracker, mindMapValidator, promptTemplate);
    }

    @Bean
    public OpenAISummarizationClient openAISummarizationClient(
            ChatClient.Builder chatBuilder,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader,
            FailureTracker failureTracker) {
        var userPromptResource = resourceLoader.getResource(promptProperties.getSummarizationUser());
        String userPromptText = copyToString(userPromptResource);

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(templateRenderer)
                .template(userPromptText)
                .build();

        var systemPromptResource = resourceLoader.getResource(promptProperties.getSummarizationSystem());
        ChatClient chatClient = chatBuilder.defaultSystem(systemPromptResource).build();

        return new OpenAISummarizationClient(chatClient, promptTemplate, failureTracker);
    }

    @Bean
    public OpenAITopicExtractionClient openAITopicExtractionClient(
            ChatClient.Builder chatBuilder,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader,
            FailureTracker failureTracker) {
        var userPromptResource = resourceLoader.getResource(promptProperties.getTopicExtractionUser());
        String userPromptText = copyToString(userPromptResource);

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(templateRenderer)
                .template(userPromptText)
                .build();

        var systemPromptResource = resourceLoader.getResource(promptProperties.getTopicExtractionSystem());
        ChatClient chatClient = chatBuilder.defaultSystem(systemPromptResource).build();

        return new OpenAITopicExtractionClient(chatClient, promptTemplate, failureTracker);
    }

    private String copyToString(Resource resource) {
        try (var in = resource.getInputStream();
                var reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {

            return FileCopyUtils.copyToString(reader);

        } catch (IOException e) {
            throw new PromptTemplateLoadException("Failed to load prompt template: " + resource.getDescription(), e);
        }
    }
}
