package ch.supsi.documentapplication.service.clients;

import ch.supsi.documentapplication.configuration.properties.EvalServiceProperties;
import ch.supsi.documentapplication.model.evaluation.CreateEvaluationRequest;
import ch.supsi.documentapplication.model.evaluation.EvalMetricName;
import ch.supsi.documentapplication.model.evaluation.MetricConfig;
import ch.supsi.documentapplication.model.learn.Topic;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Log4j2
@Component
public class EvaluationRestClient {

    private final RestClient evalServiceRestClient;
    private final EvalServiceProperties evalServiceProperties;

    public EvaluationRestClient(
            @Qualifier("evalServiceRestClient") RestClient evalServiceRestClient,
            EvalServiceProperties evalServiceProperties) {
        this.evalServiceRestClient = evalServiceRestClient;
        this.evalServiceProperties = evalServiceProperties;
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
            log.error("Failed to contact evaluation service: {}", exception.getMessage(), exception.getCause());
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
                List.of(MetricConfig.of(EvalMetricName.RELEVANCE), MetricConfig.of(EvalMetricName.TOPIC_GRANULARITY))));
    }

    public void evaluateTopicConsistency(String pageSummary, Topic topic) {
        submitAsync(CreateEvaluationRequest.of(
                "topic", pageSummary, formatTopic(topic), List.of(MetricConfig.of(EvalMetricName.CONSISTENCY))));
    }

    private static String formatTopicList(List<Topic> topics) {
        var builder = new StringBuilder();
        for (int i = 0; i < topics.size(); i++) {
            if (i > 0) {
                builder.append('\n');
            }
            builder.append(i + 1).append(". ").append(formatTopic(topics.get(i)));
        }
        return builder.toString();
    }

    private static String formatTopic(Topic topic) {
        return topic.getTitle() + "\n" + topic.getSummary();
    }
}
