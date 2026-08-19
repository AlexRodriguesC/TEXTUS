package ch.supsi.documentapplication.model.evaluation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateEvaluationRequest(
        @JsonProperty("content_type") String contentType,
        @JsonProperty("input") String input,
        @JsonProperty("actual_output") String actualOutput,
        @JsonProperty("expected_output") String expectedOutput,
        @JsonProperty("retrieval_context") List<String> retrievalContext,
        @JsonProperty("metrics") List<MetricConfig> metrics) {

    public static CreateEvaluationRequest of(
            String contentType, String input, String actualOutput, List<MetricConfig> metrics) {
        return new CreateEvaluationRequest(contentType, input, actualOutput, null, null, metrics);
    }
}
