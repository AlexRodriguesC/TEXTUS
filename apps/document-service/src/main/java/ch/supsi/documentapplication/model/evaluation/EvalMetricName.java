package ch.supsi.documentapplication.model.evaluation;

public enum EvalMetricName {
    HALLUCINATION("hallucination"),
    TOPIC_COVERAGE("topic_coverage"),
    TOPIC_GRANULARITY("topic_granularity"),
    QUESTION_RELEVANCE("question_relevance"),
    ANSWER_RELEVANCY("answer_relevancy");

    private final String value;

    EvalMetricName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
