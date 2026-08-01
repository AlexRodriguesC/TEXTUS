package ch.supsi.documentapplication.evaluation.summarization

import ch.supsi.documentapplication.exception.PromptTemplateLoadException
import ch.supsi.documentapplication.model.AiContext
import ch.supsi.documentapplication.service.RetryTemplateWrapper
import ch.supsi.documentapplication.service.clients.OpenAISummarizationClient
import com.openai.client.OpenAIClient
import com.openai.client.OpenAIClientAsync
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.setup.OpenAiSetup
import org.springframework.ai.template.st.StTemplateRenderer
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.retry.RetryPolicy
import org.springframework.core.retry.RetryTemplate
import org.springframework.util.FileCopyUtils

import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.Duration

/**
 * Focused factory that builds a production-equivalent {@link OpenAISummarizationClient}
 * without starting the database-backed application context.
 */
final class SummarizationClientEvaluationConfiguration {

    static final String DEFAULT_MODEL = "anthropic/claude-haiku-4-5-free"
    static final List<String> DEFAULT_FALLBACKS = List.of(
            "claude-haiku-4-5-20251001",
            "openai/gpt-5.4-nano")
    static final String DEFAULT_PROVIDER = "llmgateway"
    static final String SUMMARIZATION_SYSTEM =
            "classpath:/prompts/summarization/summarization-system-v1.st"
    static final String SUMMARIZATION_USER =
            "classpath:/prompts/summarization/summarization-user-v1.st"

    private SummarizationClientEvaluationConfiguration() {}

    static EvaluationClientContext create(
            ResourceLoader resourceLoader = new DefaultResourceLoader(),
            Duration timeout = Duration.ofMinutes(2)) {
        String apiKey = requireEnv("OPENAI_API_KEY")
        String baseUrl = requireEnv("OPENAI_BASE_URL")
        String model = firstNonBlank(System.getenv("SPRING_AI_OPENAI_CHAT_MODEL"), DEFAULT_MODEL)
        String provider = firstNonBlank(System.getenv("APP_AI_PROVIDER"), DEFAULT_PROVIDER)
        List<String> fallbacks = parseFallbacks(System.getenv("APP_AI_MODEL_FALLBACKS"))

        List<String> models = new ArrayList<>()
        models.add(model)
        models.addAll(fallbacks)
        if (models.size() < 3) {
            throw new IllegalStateException(
                    "At least 3 models must be configured (primary + 2 fallbacks). "
                            + "Set SPRING_AI_OPENAI_CHAT_MODEL and APP_AI_MODEL_FALLBACKS.")
        }

        AiContext aiContext = new AiContext(provider, model, List.copyOf(models))
        ChatClient chatClient = createChatClient(apiKey, baseUrl, timeout)
        RetryTemplateWrapper retryTemplate = createRetryTemplate()
        StTemplateRenderer renderer = StTemplateRenderer.builder()
                .startDelimiterToken('<' as char)
                .endDelimiterToken('>' as char)
                .build()

        String userPromptText = copyToString(resourceLoader.getResource(SUMMARIZATION_USER))
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(renderer)
                .template(userPromptText)
                .build()
        String systemPromptText = copyToString(resourceLoader.getResource(SUMMARIZATION_SYSTEM))

        OpenAISummarizationClient client = new OpenAISummarizationClient(
                chatClient,
                retryTemplate,
                systemPromptText,
                promptTemplate,
                aiContext)

        return new EvaluationClientContext(client, model, aiContext)
    }

    private static ChatClient createChatClient(String apiKey, String baseUrl, Duration timeout) {
        // OpenAiSetup expects primitive boolean/int args; Groovy null for maxRetries breaks resolution.
        boolean microsoftFoundry = false
        boolean gitHubModels = false
        int maxRetries = 2

        OpenAIClient openAiClient = OpenAiSetup.setupSyncClient(
                baseUrl,
                apiKey,
                null,
                null,
                null,
                null,
                microsoftFoundry,
                gitHubModels,
                null,
                timeout,
                maxRetries,
                null,
                null)
        OpenAIClientAsync openAiClientAsync = OpenAiSetup.setupAsyncClient(
                baseUrl,
                apiKey,
                null,
                null,
                null,
                null,
                microsoftFoundry,
                gitHubModels,
                null,
                timeout,
                maxRetries,
                null,
                null)

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiClient(openAiClient)
                .openAiClientAsync(openAiClientAsync)
                .build()
        return ChatClient.builder(chatModel).build()
    }

    private static RetryTemplateWrapper createRetryTemplate() {
        def retryPolicy = RetryPolicy.builder()
                .maxRetries(2)
                .delay(Duration.ofMillis(200))
                .build()
        return new RetryTemplateWrapper(new RetryTemplate(retryPolicy))
    }

    private static List<String> parseFallbacks(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT_FALLBACKS
        }
        return raw.split(",")
                .collect { it.trim() }
                .findAll { !it.isEmpty() }
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name)
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "${name} is required for classifier evaluation. Set it in the environment or ../../.env")
        }
        return value.trim()
    }

    private static String firstNonBlank(String primary, String fallback) {
        return (primary != null && !primary.isBlank()) ? primary.trim() : fallback
    }

    private static String copyToString(Resource resource) {
        try (def inStream = resource.getInputStream()
             def reader = new InputStreamReader(inStream, StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader)
        } catch (IOException e) {
            throw new PromptTemplateLoadException(
                    "Failed to load prompt template: ${resource.getDescription()}", e)
        }
    }

    static final class EvaluationClientContext {
        final OpenAISummarizationClient client
        final String modelName
        final AiContext aiContext

        EvaluationClientContext(OpenAISummarizationClient client, String modelName, AiContext aiContext) {
            this.client = client
            this.modelName = modelName
            this.aiContext = aiContext
        }
    }
}
