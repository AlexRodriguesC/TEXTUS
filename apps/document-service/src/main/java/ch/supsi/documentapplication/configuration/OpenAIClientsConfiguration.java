package ch.supsi.documentapplication.configuration;

import ch.supsi.documentapplication.configuration.properties.PromptProperties;
import ch.supsi.documentapplication.exception.PromptTemplateLoadException;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.clients.*;
import ch.supsi.documentapplication.service.clients.OpenAIMindMapClient;
import ch.supsi.documentapplication.service.clients.exercise.*;
import ch.supsi.documentapplication.service.validator.ChapterValidator;
import ch.supsi.documentapplication.service.validator.MindMapValidator;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;
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
    RetryTemplateWrapper retryTemplateWrapper(RetryTemplate retryTemplate) {
        return new RetryTemplateWrapper(retryTemplate);
    }

    @Bean
    public OpenAIChapterClient openAIChapterClient(
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
            StTemplateRenderer templateRenderer,
            PromptProperties promptProperties,
            ResourceLoader resourceLoader) {
        var userPromptResource = resourceLoader.getResource(promptProperties.getChapterExtractionUser());
        String userPromptText = copyToString(userPromptResource);

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(templateRenderer)
                .template(userPromptText)
                .build();

        var systemPromptResource = resourceLoader.getResource(promptProperties.getChapterExtractionSystem());
        ChatClient chatClient = chatBuilder.defaultSystem(systemPromptResource).build();

        var chapterValidator = new ChapterValidator();

        return new OpenAIChapterClient(chatClient, retryTemplateWrapper, chapterValidator, promptTemplate);
    }

    @Bean
    public OpenAIMindMapClient mindMapClient(
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
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

        return new OpenAIMindMapClient(chatClient, retryTemplateWrapper, mindMapValidator, promptTemplate);
    }

    @Bean
    public OpenAISummarizationClient openAISummarizationClient(
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader) {
        return createPromptClient(
                chatBuilder,
                templateRenderer,
                resourceLoader,
                promptProperties.getSummarizationUser(),
                promptProperties.getSummarizationSystem(),
                (client, template) -> new OpenAISummarizationClient(client, retryTemplateWrapper, template));
    }

    @Bean
    public OpenAITopicExtractionClient openAITopicExtractionClient(
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader) {
        return createPromptClient(
                chatBuilder,
                templateRenderer,
                resourceLoader,
                promptProperties.getTopicExtractionUser(),
                promptProperties.getTopicExtractionSystem(),
                (client, template) -> new OpenAITopicExtractionClient(client, retryTemplateWrapper, template));
    }

    @Bean
    public OpenAIQuestionClient openAIQuestionClient(
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader) {
        return createPromptClient(
                chatBuilder,
                templateRenderer,
                resourceLoader,
                promptProperties.getQuestionUser(),
                promptProperties.getQuestionSystem(),
                (client, template) -> new OpenAIQuestionClient(client, retryTemplateWrapper, template));
    }

    @Bean
    public OpenAIListClient openAIListClient(
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader) {
        return createPromptClient(
                chatBuilder,
                templateRenderer,
                resourceLoader,
                promptProperties.getListUser(),
                promptProperties.getListSystem(),
                (client, template) -> new OpenAIListClient(client, retryTemplateWrapper, template));
    }

    @Bean
    public OpenAIGroupClient openAIGroupClient(
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader) {
        return createPromptClient(
                chatBuilder,
                templateRenderer,
                resourceLoader,
                promptProperties.getGroupUser(),
                promptProperties.getGroupSystem(),
                (client, template) -> new OpenAIGroupClient(client, retryTemplateWrapper, template));
    }

    @Bean
    public OpenAIPairClient openAIPairClient(
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader) {
        return createPromptClient(
                chatBuilder,
                templateRenderer,
                resourceLoader,
                promptProperties.getPairUser(),
                promptProperties.getPairSystem(),
                (client, template) -> new OpenAIPairClient(client, retryTemplateWrapper, template));
    }

    @Bean
    public OpenAIExerciseFeedbackClient openAIExerciseFeedbackClient(
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader) {
        return createPromptClient(
                chatBuilder,
                templateRenderer,
                resourceLoader,
                promptProperties.getFeedbackUser(),
                promptProperties.getFeedbackSystem(),
                (client, template) -> new OpenAIExerciseFeedbackClient(client, retryTemplateWrapper, template));
    }

    private <T> T createPromptClient(
            ChatClient.Builder chatBuilder,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader,
            String userPromptPath,
            String systemPromptPath,
            BiFunction<ChatClient, PromptTemplate, T> factory) {

        var userPromptResource = resourceLoader.getResource(userPromptPath);
        String userPromptText = copyToString(userPromptResource);

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(templateRenderer)
                .template(userPromptText)
                .build();

        var systemPromptResource = resourceLoader.getResource(systemPromptPath);
        ChatClient chatClient = chatBuilder.defaultSystem(systemPromptResource).build();

        return factory.apply(chatClient, promptTemplate);
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
