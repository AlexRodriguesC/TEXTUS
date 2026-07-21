package ch.supsi.documentapplication.configuration;

import ch.supsi.documentapplication.configuration.properties.AiProperties;
import ch.supsi.documentapplication.configuration.properties.PromptProperties;
import ch.supsi.documentapplication.exception.PromptTemplateLoadException;
import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import ch.supsi.documentapplication.service.clients.*;
import ch.supsi.documentapplication.service.clients.OpenAIMindMapClient;
import ch.supsi.documentapplication.service.clients.exercise.*;
import ch.supsi.documentapplication.service.mindmap.MindMapAssembler;
import ch.supsi.documentapplication.service.validator.ChapterValidator;
import ch.supsi.documentapplication.service.validator.LabelMindMapValidator;
import ch.supsi.documentapplication.service.validator.MindMapValidator;
import com.openai.client.OpenAIClient;
import com.openai.client.OpenAIClientAsync;
import io.micrometer.observation.ObservationRegistry;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.function.TriFunction;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiCommonProperties;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.setup.OpenAiSetup;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
    public AiContext aiContext(
            AiProperties aiProperties, @Value("${spring.ai.openai.chat.model}") String defaultModel) {
        var models = new java.util.ArrayList<String>();
        models.add(defaultModel);
        models.addAll(aiProperties.getModelFallbacks());

        if (models.size() < 3) {
            throw new IllegalStateException("At least 3 models must be configured (primary + 2 fallbacks). "
                    + "Set spring.ai.openai.chat.model and app.ai.model-fallbacks with at least 2 entries.");
        }

        return new AiContext(aiProperties.getProvider(), defaultModel, List.copyOf(models));
    }

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
    @Qualifier("longRunningChatClient")
    ChatClient longRunningChatClient(
            OpenAiCommonProperties commonProperties,
            OpenAiChatProperties chatProperties,
            ToolCallingManager toolCallingManager,
            ObjectProvider<ObservationRegistry> observationRegistry,
            ObjectProvider<DefaultToolExecutionEligibilityPredicate> toolExecutionEligibilityPredicate,
            AiProperties aiProperties) {
        Duration timeout = aiProperties.getLongRunningRequestTimeout();
        OpenAIClient openAiClient = createOpenAiClient(commonProperties, timeout);
        OpenAIClientAsync openAiClientAsync = createOpenAiClientAsync(commonProperties, timeout);

        var chatModel = OpenAiChatModel.builder()
                .openAiClient(openAiClient)
                .openAiClientAsync(openAiClientAsync)
                .options(chatProperties.toOptions())
                .toolCallingManager(toolCallingManager)
                .observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
                .toolExecutionEligibilityPredicate(
                        toolExecutionEligibilityPredicate.getIfUnique(DefaultToolExecutionEligibilityPredicate::new))
                .build();

        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public OpenAIChapterClient openAIChapterClient(
            ObjectMapper objectMapper,
            @Qualifier("longRunningChatClient") ChatClient chatClient,
            RetryTemplateWrapper retryTemplateWrapper,
            StTemplateRenderer templateRenderer,
            PromptProperties promptProperties,
            ResourceLoader resourceLoader,
            AiContext aiContext) {

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

        var chapterValidator = new ChapterValidator();

        return new OpenAIChapterClient(
                objectMapper,
                chatClient,
                retryTemplateWrapper,
                systemPromptText,
                promptTemplate,
                feedbackSystemPromptText,
                feedbackPromptTemplate,
                chapterValidator,
                aiContext);
    }

    @Bean
    public OpenAIMindMapClient mindMapClient(
            ObjectMapper objectMapper,
            @Qualifier("longRunningChatClient") ChatClient chatClient,
            RetryTemplateWrapper retryTemplateWrapper,
            StTemplateRenderer templateRenderer,
            PromptProperties promptProperties,
            ResourceLoader resourceLoader,
            AiContext aiContext) {
        var userPromptResource = resourceLoader.getResource(promptProperties.getMindMapExtractionUser());
        String userPromptText = copyToString(userPromptResource);

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(templateRenderer)
                .template(userPromptText)
                .build();

        var systemPromptResource = resourceLoader.getResource(promptProperties.getMindMapExtractionSystem());
        String systemPromptText = copyToString(systemPromptResource);

        var feedbackUserPromptResource =
                resourceLoader.getResource(promptProperties.getMindMapExtractionFeedbackUser());
        String feedbackUserPromptText = copyToString(feedbackUserPromptResource);
        PromptTemplate feedbackPromptTemplate = PromptTemplate.builder()
                .renderer(templateRenderer)
                .template(feedbackUserPromptText)
                .build();

        var feedbackSystemPromptResource =
                resourceLoader.getResource(promptProperties.getMindMapExtractionFeedbackSystem());
        String feedbackSystemPromptText = copyToString(feedbackSystemPromptResource);

        return new OpenAIMindMapClient(
                objectMapper,
                chatClient,
                retryTemplateWrapper,
                systemPromptText,
                promptTemplate,
                feedbackSystemPromptText,
                feedbackPromptTemplate,
                new MindMapAssembler(),
                new LabelMindMapValidator(),
                new MindMapValidator(),
                aiContext);
    }

    @Bean
    public OpenAISummarizationClient openAISummarizationClient(
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader,
            AiContext aiContext) {
        return createPromptClient(
                chatBuilder,
                templateRenderer,
                resourceLoader,
                promptProperties.getSummarizationUser(),
                promptProperties.getSummarizationSystem(),
                (client, systemPrompt, template) ->
                        new OpenAISummarizationClient(client, retryTemplateWrapper, systemPrompt, template, aiContext));
    }

    @Bean
    public OpenAITopicExtractionClient openAITopicExtractionClient(
            ObjectMapper objectMapper,
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader,
            AiContext aiContext) {
        return createPromptClient(
                chatBuilder,
                templateRenderer,
                resourceLoader,
                promptProperties.getTopicExtractionUser(),
                promptProperties.getTopicExtractionSystem(),
                (client, systemPrompt, template) -> new OpenAITopicExtractionClient(
                        objectMapper, client, retryTemplateWrapper, systemPrompt, template, aiContext));
    }

    @Bean
    public OpenAIQuestionClient openAIQuestionClient(
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader,
            AiContext aiContext) {
        return createPromptClient(
                chatBuilder,
                templateRenderer,
                resourceLoader,
                promptProperties.getQuestionUser(),
                promptProperties.getQuestionSystem(),
                (client, systemPrompt, template) ->
                        new OpenAIQuestionClient(client, retryTemplateWrapper, systemPrompt, template, aiContext));
    }

    @Bean
    public OpenAIListClient openAIListClient(
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader,
            AiContext aiContext) {
        return createPromptClient(
                chatBuilder,
                templateRenderer,
                resourceLoader,
                promptProperties.getListUser(),
                promptProperties.getListSystem(),
                (client, systemPrompt, template) ->
                        new OpenAIListClient(client, retryTemplateWrapper, systemPrompt, template, aiContext));
    }

    @Bean
    public OpenAIGroupClient openAIGroupClient(
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader,
            AiContext aiContext) {
        return createPromptClient(
                chatBuilder,
                templateRenderer,
                resourceLoader,
                promptProperties.getGroupUser(),
                promptProperties.getGroupSystem(),
                (client, systemPrompt, template) ->
                        new OpenAIGroupClient(client, retryTemplateWrapper, systemPrompt, template, aiContext));
    }

    @Bean
    public OpenAIPairClient openAIPairClient(
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader,
            AiContext aiContext) {
        return createPromptClient(
                chatBuilder,
                templateRenderer,
                resourceLoader,
                promptProperties.getPairUser(),
                promptProperties.getPairSystem(),
                (client, systemPrompt, template) ->
                        new OpenAIPairClient(client, retryTemplateWrapper, systemPrompt, template, aiContext));
    }

    @Bean
    public OpenAIExerciseFeedbackClient openAIExerciseFeedbackClient(
            ChatClient.Builder chatBuilder,
            RetryTemplateWrapper retryTemplateWrapper,
            PromptProperties promptProperties,
            StTemplateRenderer templateRenderer,
            ResourceLoader resourceLoader,
            AiContext aiContext) {
        return createPromptClient(
                chatBuilder,
                templateRenderer,
                resourceLoader,
                promptProperties.getFeedbackUser(),
                promptProperties.getFeedbackSystem(),
                (client, systemPrompt, template) -> new OpenAIExerciseFeedbackClient(
                        client, retryTemplateWrapper, systemPrompt, template, aiContext));
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

    private static OpenAIClient createOpenAiClient(OpenAiCommonProperties commonProperties, Duration timeout) {
        return OpenAiSetup.setupSyncClient(
                commonProperties.getBaseUrl(),
                commonProperties.getApiKey(),
                commonProperties.getCredential(),
                commonProperties.getMicrosoftDeploymentName(),
                commonProperties.getMicrosoftFoundryServiceVersion(),
                commonProperties.getOrganizationId(),
                commonProperties.isMicrosoftFoundry(),
                commonProperties.isGitHubModels(),
                commonProperties.getModel(),
                timeout,
                commonProperties.getMaxRetries(),
                commonProperties.getProxy(),
                commonProperties.getCustomHeaders());
    }

    private static OpenAIClientAsync createOpenAiClientAsync(
            OpenAiCommonProperties commonProperties, Duration timeout) {
        return OpenAiSetup.setupAsyncClient(
                commonProperties.getBaseUrl(),
                commonProperties.getApiKey(),
                commonProperties.getCredential(),
                commonProperties.getMicrosoftDeploymentName(),
                commonProperties.getMicrosoftFoundryServiceVersion(),
                commonProperties.getOrganizationId(),
                commonProperties.isMicrosoftFoundry(),
                commonProperties.isGitHubModels(),
                commonProperties.getModel(),
                timeout,
                commonProperties.getMaxRetries(),
                commonProperties.getProxy(),
                commonProperties.getCustomHeaders());
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
