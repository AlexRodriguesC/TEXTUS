package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.model.Topic;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.ParameterizedTypeReference;
import tools.jackson.core.exc.StreamReadException;

@Slf4j
public class OpenAITopicExtractionClient extends Client<Pair<Page, List<Topic>>, List<Topic>> {

    private static final String PAGE_TEXT = "page_text";
    private static final String EXISTING_LABELS = "existing_labels";

    public OpenAITopicExtractionClient(
            ChatClient chatClient, PromptTemplate promptTemplate, FailureTracker failureTracker) {
        super(chatClient, promptTemplate, failureTracker);
    }

    @Override
    protected Optional<List<Topic>> call(Pair<Page, List<Topic>> input) {
        var page = input.getLeft();
        var existingTopics = input.getRight();
        var distinctLabels = existingTopics.stream()
                .flatMap(topic -> topic.getLabels().stream())
                .distinct()
                .toList();

        Map<String, Object> params = Map.of(PAGE_TEXT, page.getSummary(), EXISTING_LABELS, distinctLabels);
        var prompt = getPrompt(params);

        try {
            var output = chatClient.prompt(prompt).call().entity(new ParameterizedTypeReference<List<Topic>>() {});
            return Optional.ofNullable(output);
        } catch (StreamReadException e) {
            log.warn("Failed to parse output for topic exctraction prompt", e);
            return Optional.empty();
        }
    }
}
