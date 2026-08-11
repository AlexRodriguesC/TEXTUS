package ch.supsi.documentapplication.model.evaluation;

public enum EvalMetricName {
    CONSISTENCY("consistency"),
    RELEVANCE("relevance"),
    TOPIC_GRANULARITY("topic_granularity");

    private final String value;

    EvalMetricName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
