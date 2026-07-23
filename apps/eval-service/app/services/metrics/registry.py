"""Registry of available DeepEval metrics and their metadata."""

from app.models.domain import MetricDefinition

METRIC_REGISTRY: dict[str, MetricDefinition] = {
  "hallucination": MetricDefinition(
    name="hallucination",
    description=(
      "DeepEval: whether the summary contradicts the source text "
      "(input=text, actual_output=summary). "
      "Score 0 = no hallucination, 1 = fully hallucinated."
    ),
    required_params=[],
  ),
  "topic_coverage": MetricDefinition(
    name="topic_coverage",
    description=(
      "G-Eval: whether the topic list covers all subjects in the summary "
      "(input=summary, actual_output=topic list)."
    ),
    required_params=[],
  ),
  "topic_granularity": MetricDefinition(
    name="topic_granularity",
    description=(
      "G-Eval: whether each topic is distinct, specific, and non-redundant "
      "(actual_output=topic list)."
    ),
    required_params=[],
  ),
  "question_relevance": MetricDefinition(
    name="question_relevance",
    description=(
      "G-Eval: whether the generated question is relevant and answerable from "
      "the topic (input=topic, actual_output=question)."
    ),
    required_params=[],
  ),
  "answer_relevancy": MetricDefinition(
    name="answer_relevancy",
    description=(
      "DeepEval: whether the generated question is relevant to the topic "
      "(input=topic, actual_output=question)."
    ),
    required_params=[],
  ),
}


def get_metric_definition(name: str) -> MetricDefinition | None:
  return METRIC_REGISTRY.get(name)


def list_metric_definitions() -> list[MetricDefinition]:
  return list(METRIC_REGISTRY.values())
