package ch.supsi.textusbackend.service.clients.enrichment;

import ch.supsi.textusbackend.model.Chunk;
import ch.supsi.textusbackend.model.Entity;
import ch.supsi.textusbackend.properties.PromptProperties;
import ch.supsi.textusbackend.service.clients.OpenAIClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;

@Component
@Slf4j
public class OpenAIEnrichmentClient extends OpenAIClient implements EnrichmentClient {

    private final Resource userPromptResource;

    @Autowired
    public OpenAIEnrichmentClient(
            ChatClient.Builder chatBuilder, PromptProperties promptProperties, ResourceLoader resourceLoader) {
        super(chatBuilder
                .defaultSystem(resourceLoader.getResource(promptProperties.getEnrichmentSystem()))
                .build());
        this.userPromptResource = resourceLoader.getResource(promptProperties.getEnrichmentUser());
    }

    @Override
    public Chunk enrich(@Nullable String previousSummary, @NotNull String toEnrich, @NotNull List<String> rollingKeys) {
        var userPromptText = load(userPromptResource);
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder()
                        .startDelimiterToken('<')
                        .endDelimiterToken('>')
                        .build())
                .template(userPromptText)
                .build();

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
        int currentAttempts = 0;
        int maxAttempts = 5;
        Chunk chunk = null;

        while (!successful) {
            try {
                chunk = chatClient.prompt(prompt).call().entity(Chunk.class);
                successful = true;
            } catch (JacksonException e) {
                currentAttempts++;
                log.debug("Failed to map LLM prompt to {}, attempting new extraction...", Chunk.class, e);
                if (currentAttempts > maxAttempts) throw e;
            }
        }

        return chunk == null ? null : Chunk.from(chunk, normaliseEntities(chunk));
    }

    private List<Entity> normaliseEntities(Chunk chunk) {
        var normalisedEntities = new ArrayList<Entity>();
        var rawEntities = chunk.entities();

        for (Entity entity : rawEntities) {
            normalisedEntities.add(entity.validate());
        }
        return normalisedEntities;
    }
}
