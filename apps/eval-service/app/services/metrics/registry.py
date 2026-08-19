"""Registry of available DeepEval metrics and their metadata."""

from app.models.domain import MetricDefinition

METRIC_REGISTRY: dict[str, MetricDefinition] = {
    "hallucination": MetricDefinition(
        name="hallucination",
        description=(
            "DeepEval HallucinationMetric: fraction of context items contradicted "
            "by Actual Output (context=[input], actual_output=generated content). "
            "Score 0-1, lower is better; default max threshold 0.5."
        ),
        required_params=[],
    ),
    "relevance": MetricDefinition(
        name="relevance",
        description=(
            "G-Eval SummEval Relevance: selection of important content from Input "
            "(input=source, actual_output=topic list). Score 1-5."
        ),
        required_params=[],
    )
}


def get_metric_definition(name: str) -> MetricDefinition | None:
    return METRIC_REGISTRY.get(name)


def list_metric_definitions() -> list[MetricDefinition]:
    return list(METRIC_REGISTRY.values())
