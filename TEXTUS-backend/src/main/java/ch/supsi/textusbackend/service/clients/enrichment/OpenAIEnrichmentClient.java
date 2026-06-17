package ch.supsi.textusbackend.service.clients.enrichment;

import ch.supsi.textusbackend.model.Chunk;
import ch.supsi.textusbackend.properties.PromptProperties;
import ch.supsi.textusbackend.service.clients.OpenAIClient;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.TemplateRenderer;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;

@Component
@Slf4j
public class OpenAIEnrichmentClient extends OpenAIClient implements EnrichmentClient {

    private final Resource userPromptResource;
    private final TokenCountEstimator tokenCountEstimator;
    private final TemplateRenderer templateRenderer;

    @Autowired
    public OpenAIEnrichmentClient(
            ChatClient.Builder chatBuilder,
            PromptProperties promptProperties,
            ResourceLoader resourceLoader,
            TokenCountEstimator tokenCountEstimator) {
        super(chatBuilder
                .defaultSystem(resourceLoader.getResource(promptProperties.getEnrichmentSystem()))
                .build());
        this.userPromptResource = resourceLoader.getResource(promptProperties.getEnrichmentUser());
        this.tokenCountEstimator = tokenCountEstimator;
        this.templateRenderer = StTemplateRenderer.builder()
                .startDelimiterToken('<')
                .endDelimiterToken('>')
                .build();
    }

    @Override
    public Chunk enrich(@Nullable String previousSummary, @NotNull String toEnrich, @NotNull List<String> rollingKeys) {
        var userPromptText = load(userPromptResource);

        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(templateRenderer)
                .template(userPromptText)
                .build();

        var filledUserPrompt = promptTemplate.render(Map.of(
                "prev_summary",
                previousSummary == null ? "N/A" : previousSummary,
                "page_text",
                toEnrich,
                "rolling_keys",
                rollingKeys));

        var tokensIn = tokenCountEstimator.estimate(filledUserPrompt);
        var userPrompt = UserMessage.builder().text(filledUserPrompt).build();
        var prompt = new Prompt(userPrompt);

        boolean successful = false;
        int currentAttempts = 0;
        int maxAttempts = 5;
        Chunk chunk = null;

        var tokensOut = 0;
        while (!successful) {
            try {
                chunk = chatClient.prompt(prompt).call().entity(Chunk.class);
                tokensOut = tokenCountEstimator.estimate(chunk.toString());
                successful = true;
            } catch (JacksonException e) {
                currentAttempts++;
                log.debug(
                        "Failed to map LLM prompt to {}. Attempt number: {}, attempting new extraction...",
                        Chunk.class,
                        currentAttempts,
                        e);
                if (currentAttempts > maxAttempts) throw e;
            }
        }

        chunk.setTokenCountInput(tokensIn);
        chunk.setTokenCountOutput(tokensOut);
        chunk.normaliseEntities();

        return chunk;
    }
}
