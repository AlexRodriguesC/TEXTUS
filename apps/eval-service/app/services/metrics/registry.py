"""Registry of available DeepEval metrics and their metadata."""

from app.models.domain import MetricDefinition

METRIC_REGISTRY: dict[str, MetricDefinition] = {
  "summarization": MetricDefinition(
    name="summarization",
    description="Evaluates how well the actual output summarizes the input.",
    required_params=[],
  ),
  "answer_relevancy": MetricDefinition(
    name="answer_relevancy",
    description="Evaluates how relevant the actual output is to the input.",
    required_params=[],
  ),
  "geval": MetricDefinition(
    name="geval",
    description="Custom G-Eval metric using criteria or evaluation steps.",
    required_params=["criteria or evaluation_steps"],
  ),
}


def get_metric_definition(name: str) -> MetricDefinition | None:
  return METRIC_REGISTRY.get(name)


def list_metric_definitions() -> list[MetricDefinition]:
  return list(METRIC_REGISTRY.values())
