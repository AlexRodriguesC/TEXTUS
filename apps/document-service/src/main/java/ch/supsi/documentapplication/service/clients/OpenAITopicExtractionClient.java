package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.ParameterizedTypeReference;

@Log4j2
public class OpenAITopicExtractionClient extends Client<Pair<Page, List<Topic>>, List<Topic>> {

    private static final String PAGE_TEXT = "page_text";
    private static final String EXISTING_LABELS = "existing_labels";

    public OpenAITopicExtractionClient(
            ChatClient chatClient, RetryTemplateWrapper retryTemplate, PromptTemplate promptTemplate) {
        super(chatClient, retryTemplate, promptTemplate);
    }

    @Override
    protected List<Topic> call(Pair<Page, List<Topic>> input) {
        var page = input.getLeft();
        var existingTopics = input.getRight();
        var distinctLabels = existingTopics.stream()
                .flatMap(topic -> topic.getLabels().stream())
                .distinct()
                .toList();

        Map<String, Object> params = Map.of(PAGE_TEXT, page.getSummary(), EXISTING_LABELS, distinctLabels);
        var prompt = getPrompt(params);

        return chatClient.prompt(prompt).call().entity(new ParameterizedTypeReference<>() {});
    }
}
