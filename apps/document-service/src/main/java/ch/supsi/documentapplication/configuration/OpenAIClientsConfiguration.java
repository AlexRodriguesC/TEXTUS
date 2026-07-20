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
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.function.TriFunction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.retry.*;
import org.springframework.util.FileCopyUtils;
import tools.jackson.databind.ObjectMapper;

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
            ObjectMapper objectMapper,
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
        String systemPromptText = copyToString(systemPromptResource);

        var feedbackUserPromptResource =
                resourceLoader.getResource(promptProperties.getChapterExtractionFeedbackUser());
        String feedbackUserPromptText = copyToString(feedbackUserPromptResource);
        PromptTemplate feedbackPromptTemplate = PromptTemplate.builder()
                .renderer(templateRenderer)
                .template(feedbackUserPromptText)
                .build();

        var feedbackSystemPromptResource =
                resourceLoader.getResource(promptProperties.getChapterExtractionFeedbackSystem());
        String feedbackSystemPromptText = copyToString(feedbackSystemPromptResource);

        ChatClient chatClient = chatBuilder.build();

        var chapterValidator = new ChapterValidator();

        return new OpenAIChapterClient(
                objectMapper,
                chatClient,
                retryTemplateWrapper,
                systemPromptText,
                promptTemplate,
                feedbackSystemPromptText,
                feedbackPromptTemplate,
                chapterValidator);
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
        String systemPromptText = copyToString(systemPromptResource);
        ChatClient chatClient = chatBuilder.build();

        var mindMapValidator = new MindMapValidator();

        return new OpenAIMindMapClient(
                chatClient, retryTemplateWrapper, systemPromptText, promptTemplate, mindMapValidator);
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
                (client, systemPrompt, template) ->
                        new OpenAISummarizationClient(client, retryTemplateWrapper, systemPrompt, template));
    }

    @Bean
    public OpenAITopicExtractionClient openAITopicExtractionClient(
            ObjectMapper objectMapper,
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
                (client, systemPrompt, template) -> new OpenAITopicExtractionClient(
                        objectMapper, client, retryTemplateWrapper, systemPrompt, template));
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
                (client, systemPrompt, template) ->
                        new OpenAIQuestionClient(client, retryTemplateWrapper, systemPrompt, template));
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
                (client, systemPrompt, template) ->
                        new OpenAIListClient(client, retryTemplateWrapper, systemPrompt, template));
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
                (client, systemPrompt, template) ->
                        new OpenAIGroupClient(client, retryTemplateWrapper, systemPrompt, template));
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
                (client, systemPrompt, template) ->
                        new OpenAIPairClient(client, retryTemplateWrapper, systemPrompt, template));
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
                (client, systemPrompt, template) ->
                        new OpenAIExerciseFeedbackClient(client, retryTemplateWrapper, systemPrompt, template));
    }

    private <T> T createPromptClient(
            ChatClient.Builder chatBuilder,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader,
            String userPromptPath,
            String systemPromptPath,
            TriFunction<ChatClient, String, PromptTemplate, T> factory) {

        var userPromptResource = resourceLoader.getResource(userPromptPath);
        String userPromptText = copyToString(userPromptResource);

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(templateRenderer)
                .template(userPromptText)
                .build();

        var systemPromptResource = resourceLoader.getResource(systemPromptPath);
        String systemPromptText = copyToString(systemPromptResource);
        ChatClient chatClient = chatBuilder.build();

        return factory.apply(chatClient, systemPromptText, promptTemplate);
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
