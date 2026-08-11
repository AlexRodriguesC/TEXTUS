"""Registry of available DeepEval metrics and their metadata."""

from app.models.domain import MetricDefinition

METRIC_REGISTRY: dict[str, MetricDefinition] = {
  "consistency": MetricDefinition(
    name="consistency",
    description=(
      "G-Eval SummEval Consistency: factual alignment between Actual Output "
      "and Input (input=source, actual_output=generated content). Score 1-5."
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
  ),
  "topic_granularity": MetricDefinition(
    name="topic_granularity",
    description=(
      "Custom TEXTUS metric (not a SummEval dimension): whether each topic is "
      "distinct, specific, and non-redundant (actual_output=topic list)."
    ),
    required_params=[],
  ),
}


def get_metric_definition(name: str) -> MetricDefinition | None:
  return METRIC_REGISTRY.get(name)


def list_metric_definitions() -> list[MetricDefinition]:
  return list(METRIC_REGISTRY.values())
