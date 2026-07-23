package ch.supsi.documentapplication.model.evaluation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MetricConfig(@JsonProperty("name") String name, @JsonProperty("threshold") Double threshold) {

    public static MetricConfig of(EvalMetricName metric) {
        return new MetricConfig(metric.getValue(), null);
    }
}
