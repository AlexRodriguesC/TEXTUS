package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.configuration.properties.EvalServiceProperties;
import ch.supsi.documentapplication.model.evaluation.CreateEvaluationRequest;
import ch.supsi.documentapplication.model.evaluation.EvalMetricName;
import ch.supsi.documentapplication.model.evaluation.MetricConfig;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.model.learn.exercise.BaseExercise;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Log4j2
@Component
public class EvaluationRestClient {

    private final RestClient evalServiceRestClient;
    private final EvalServiceProperties evalServiceProperties;
    private final ObjectMapper objectMapper;

    public EvaluationRestClient(
            @Qualifier("evalServiceRestClient") RestClient evalServiceRestClient,
            EvalServiceProperties evalServiceProperties,
            ObjectMapper objectMapper) {
        this.evalServiceRestClient = evalServiceRestClient;
        this.evalServiceProperties = evalServiceProperties;
        this.objectMapper = objectMapper;
    }

    public void submitAsync(CreateEvaluationRequest request) {
        CompletableFuture.runAsync(() -> submit(request));
    }

    void submit(CreateEvaluationRequest request) {
        try {
            evalServiceRestClient
                    .post()
                    .uri(evalServiceProperties.getEvaluationsEndpoint())
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception exception) {
            log.error("Failed to contact evaluation service: {}", exception.getMessage(), exception);
        }
    }

    public void evaluateTopicList(String pageSummary, List<Topic> topics) {
        if (topics.isEmpty()) {
            return;
        }
        submitAsync(CreateEvaluationRequest.of(
                "topic_list",
                pageSummary,
                formatTopicList(topics),
                List.of(
                        MetricConfig.of(EvalMetricName.TOPIC_COVERAGE),
                        MetricConfig.of(EvalMetricName.TOPIC_GRANULARITY))));
    }

    public void evaluateTopicHallucination(String pageSummary, Topic topic) {
        submitAsync(CreateEvaluationRequest.of(
                "topic", pageSummary, formatTopic(topic), List.of(MetricConfig.of(EvalMetricName.HALLUCINATION))));
    }

    public void evaluateExercise(Topic topic, BaseExercise<?> exercise) {
        String topicText = formatTopic(topic);
        submitAsync(CreateEvaluationRequest.of(
                exercise.getType().name(),
                topicText,
                exercise.getRequest(),
                List.of(MetricConfig.of(EvalMetricName.QUESTION_RELEVANCE))));
        submitAsync(CreateEvaluationRequest.of(
                exercise.getType().name(),
                topicText,
                serializeAnswer(exercise.getAnswer()),
                List.of(MetricConfig.of(EvalMetricName.ANSWER_RELEVANCY))));
    }

    private static String formatTopicList(List<Topic> topics) {
        var builder = new StringBuilder();
        for (int i = 0; i < topics.size(); i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(i + 1).append(". ").append(topics.get(i).getTitle());
        }
        return builder.toString();
    }

    private static String formatTopic(Topic topic) {
        return topic.getTitle() + "\n" + topic.getSummary();
    }

    private String serializeAnswer(Object answer) {
        if (answer == null) {
            return "";
        }
        if (answer instanceof String stringAnswer) {
            return stringAnswer;
        }
        try {
            return objectMapper.writeValueAsString(answer);
        } catch (JacksonException exception) {
            log.warn("Failed to serialize exercise answer for evaluation: {}", exception.getMessage());
            return String.valueOf(answer);
        }
    }
}
