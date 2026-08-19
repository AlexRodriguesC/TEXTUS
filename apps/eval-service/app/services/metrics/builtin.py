"""Built-in DeepEval metric constructors."""

from deepeval.metrics import GEval, HallucinationMetric
from deepeval.metrics.base_metric import BaseMetric
from deepeval.models.base_model import DeepEvalBaseLLM

from app.core.exceptions import InvalidMetricConfigError
from app.models.domain import MetricConfigInput
from app.services.metrics.textus_geval import TEXTUS_GEVAL_PRESETS

HALLUCINATION_DEFAULT_THRESHOLD = 0.5


def build_hallucination_metric(
    config: MetricConfigInput,
    llm: DeepEvalBaseLLM,
) -> HallucinationMetric:
    threshold = (
        config.threshold if config.threshold is not None else HALLUCINATION_DEFAULT_THRESHOLD
    )
    return HallucinationMetric(threshold=threshold, model=llm)


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
        evaluation_steps=preset.evaluation_steps,
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
    if config.name in TEXTUS_GEVAL_PRESETS:
        return build_textus_geval_metric(config.name, config, llm)
    raise InvalidMetricConfigError(f"Unsupported built-in metric: {config.name}")
