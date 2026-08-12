package ch.supsi.documentapplication.model.evaluation;

public enum EvalMetricName {
    HALLUCINATION("hallucination"),
    RELEVANCE("relevance");

    private final String value;

    EvalMetricName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
