package ch.supsi.documentapplication.service.clients.enrichment;

import ch.supsi.documentapplication.model.Chunk;
import ch.supsi.documentapplication.service.clients.FailureTracker;
import ch.supsi.documentapplication.service.clients.OpenAIClient;
import ch.supsi.documentapplication.service.clients.StringSanitizer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

@Slf4j
public class OpenAIEnrichmentClient extends OpenAIClient implements EnrichmentClient {

    private final PromptTemplate promptTemplate;
    private final FailureTracker failureTracker;
    private final ObjectMapper chunkDeserializer;
    private final StringSanitizer stringSanitizer;

    public OpenAIEnrichmentClient(
            ChatClient chatClient,
            FailureTracker failureTracker,
            ObjectMapper chunkDeserializer,
            StringSanitizer stringSanitizer,
            PromptTemplate promptTemplate) {
        super(chatClient);
        this.promptTemplate = promptTemplate;
        this.chunkDeserializer = chunkDeserializer;
        this.failureTracker = failureTracker;
        this.stringSanitizer = stringSanitizer;
    }

    @Override
    public Chunk enrich(@Nullable String previousSummary, @NotNull String toEnrich, @NotNull List<String> rollingKeys) {
        var filledUserPrompt = promptTemplate.render(Map.of(
                "prev_summary",
                previousSummary == null ? "N/A" : previousSummary,
                "page_text",
                toEnrich,
                "rolling_keys",
                rollingKeys));

        var userPrompt = UserMessage.builder().text(filledUserPrompt).build();
        var prompt = new Prompt(userPrompt);

        boolean successful = false;
        Chunk chunk = null;

        while (!successful) {
            try {
                ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();

                String generation = Optional.ofNullable(chatResponse)
                        .map(ChatResponse::getResult)
                        .map(Generation::getOutput)
                        .map(AbstractMessage::getText)
                        .orElse(null);

                if (generation == null) {
                    failureTracker.registerFailure();
                    continue;
                }

                String sanitized = stringSanitizer.sanitize(generation);
                chunk = chunkDeserializer.readValue(sanitized, Chunk.class);

                Usage usage = chatResponse.getMetadata().getUsage();
                chunk.setTokenCountInput(usage.getPromptTokens());
                chunk.setTokenCountOutput(usage.getCompletionTokens());

                successful = true;
            } catch (JsonProcessingException e) {
                log.debug(
                        "Failed to map LLM prompt to {}. Attempting new extraction...",
                        Chunk.class.getCanonicalName(),
                        e);
                failureTracker.registerFailure();
            }
        }

        failureTracker.resetFailures();

        return chunk;
    }
}
