package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.model.AiContext;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.model.learn.ExerciseType;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.RetryTemplateWrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import tools.jackson.databind.ObjectMapper;

@Log4j2
public class OpenAITopicExtractionClient extends Client<Pair<Page, List<Topic>>, List<Topic>> {

    private final ObjectMapper objectMapper;
    private final EvaluationRestClient evaluationRestClient;

    private static final String PAGE_TEXT = "page_text";
    private static final String EXISTING_LABELS = "existing_labels";

    public OpenAITopicExtractionClient(
            ObjectMapper objectMapper,
            EvaluationRestClient evaluationRestClient,
            ChatClient chatClient,
            RetryTemplateWrapper retryTemplate,
            String systemPrompt,
            PromptTemplate promptTemplate,
            AiContext aiContext) {
        super(
                chatClient,
                new BeanOutputConverter<>(new ParameterizedTypeReference<List<Topic>>() {}),
                retryTemplate,
                systemPrompt,
                promptTemplate,
                aiContext);
        this.objectMapper = objectMapper;
        this.evaluationRestClient = evaluationRestClient;
    }

    @Override
    protected ClientResult<List<Topic>> call(Pair<Page, List<Topic>> input) {
        var page = input.getLeft();
        var existingTopics = input.getRight();
        var distinctLabels = existingTopics.stream()
                .flatMap(topic -> topic.getLabels().stream())
                .distinct()
                .toList();

        Map<String, Object> params = new HashMap<>();
        params.put(PAGE_TEXT, page.getSummary());
        params.put(EXISTING_LABELS, objectMapper.writeValueAsString(distinctLabels));
        ClientResult<List<Topic>> result = invoke(getPrompt(params), new ParameterizedTypeReference<>() {});
        List<Topic> topics = result.result();
        for (Topic topic : topics) {
            topic.setExerciseTypes(normalizeExerciseTypes(topic.getExerciseTypes()));
        }
        evaluationRestClient.evaluateTopicList(page.getSummary(), topics);
        for (Topic topic : topics) {
            evaluationRestClient.evaluateTopicHallucination(page.getSummary(), topic);
        }
        return result;
    }

    static List<ExerciseType> normalizeExerciseTypes(List<ExerciseType> exerciseTypes) {
        if (exerciseTypes == null || exerciseTypes.isEmpty()) {
            return List.of(ExerciseType.QUESTION);
        }
        List<ExerciseType> deduped = new ArrayList<>(new LinkedHashSet<>(
                exerciseTypes.stream().filter(Objects::nonNull).toList()));
        if (deduped.isEmpty()) {
            return List.of(ExerciseType.QUESTION);
        }
        return List.copyOf(deduped);
    }
}
