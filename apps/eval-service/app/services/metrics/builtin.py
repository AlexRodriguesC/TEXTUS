"""Built-in DeepEval metric constructors."""

from deepeval.metrics import AnswerRelevancyMetric, GEval, HallucinationMetric
from deepeval.metrics.base_metric import BaseMetric
from deepeval.models.base_model import DeepEvalBaseLLM

from app.core.exceptions import InvalidMetricConfigError
from app.models.domain import MetricConfigInput
from app.services.metrics.textus_geval import TEXTUS_GEVAL_PRESETS


def build_hallucination_metric(
  config: MetricConfigInput,
  llm: DeepEvalBaseLLM,
) -> HallucinationMetric:
  threshold = config.threshold if config.threshold is not None else 0.5
  return HallucinationMetric(
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


def build_textus_geval_metric(
  name: str,
  config: MetricConfigInput,
  llm: DeepEvalBaseLLM,
) -> GEval:
  preset = TEXTUS_GEVAL_PRESETS[name]
  threshold = config.threshold if config.threshold is not None else preset.default_threshold
  return GEval(
    name=name,
    criteria=preset.criteria,
    evaluation_params=preset.evaluation_params,
    rubric=preset.rubric,
    threshold=threshold,
    model=llm,
  )


def build_builtin_metric(
  config: MetricConfigInput,
  llm: DeepEvalBaseLLM,
) -> BaseMetric:
  if config.name == "hallucination":
    return build_hallucination_metric(config, llm)
  if config.name == "answer_relevancy":
    return build_answer_relevancy_metric(config, llm)
  if config.name in TEXTUS_GEVAL_PRESETS:
    return build_textus_geval_metric(config.name, config, llm)
  raise InvalidMetricConfigError(f"Unsupported built-in metric: {config.name}")
