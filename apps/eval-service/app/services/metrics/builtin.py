"""Built-in DeepEval metric constructors."""

from deepeval.metrics import AnswerRelevancyMetric, GEval, SummarizationMetric
from deepeval.metrics.base_metric import BaseMetric
from deepeval.models.base_model import DeepEvalBaseLLM

from app.core.exceptions import InvalidMetricConfigError
from app.models.domain import MetricConfigInput


def build_summarization_metric(
  config: MetricConfigInput,
  llm: DeepEvalBaseLLM,
) -> SummarizationMetric:
  threshold = config.threshold if config.threshold is not None else 0.5
  return SummarizationMetric(
    threshold=threshold,
    model=llm,
    include_reason=True,
  )


def build_answer_relevancy_metric(
  config: MetricConfigInput,
  llm: DeepEvalBaseLLM,
) -> AnswerRelevancyMetric:
  threshold = config.threshold if config.threshold is not None else 0.5
  return AnswerRelevancyMetric(
    threshold=threshold,
    model=llm,
    include_reason=True,
  )


def build_geval_metric(
  config: MetricConfigInput,
  llm: DeepEvalBaseLLM,
) -> GEval:
  if not config.criteria and not config.evaluation_steps:
    raise InvalidMetricConfigError(
      "geval metric requires either 'criteria' or 'evaluation_steps'"
    )

  threshold = config.threshold if config.threshold is not None else 0.5
  geval_kwargs: dict[str, object] = {
    "name": config.name,
    "threshold": threshold,
    "model": llm,
  }
  if config.criteria:
    geval_kwargs["criteria"] = config.criteria
  if config.evaluation_steps:
    geval_kwargs["evaluation_steps"] = config.evaluation_steps

  return GEval(**geval_kwargs)


def build_builtin_metric(
  config: MetricConfigInput,
  llm: DeepEvalBaseLLM,
) -> BaseMetric:
  if config.name == "summarization":
    return build_summarization_metric(config, llm)
  if config.name == "answer_relevancy":
    return build_answer_relevancy_metric(config, llm)
  if config.name == "geval":
    return build_geval_metric(config, llm)
  raise InvalidMetricConfigError(f"Unsupported built-in metric: {config.name}")
