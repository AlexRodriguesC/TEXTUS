"""Factory for resolving metric configurations into DeepEval metric instances."""

from deepeval.metrics.base_metric import BaseMetric
from deepeval.models.base_model import DeepEvalBaseLLM

from app.core.exceptions import MetricNotFoundError
from app.models.domain import MetricConfigInput
from app.services.metrics.builtin import build_builtin_metric
from app.services.metrics.registry import get_metric_definition


class ResolvedMetric:
  """Pairs a metric configuration with its DeepEval instance."""

  def __init__(self, config: MetricConfigInput, metric: BaseMetric) -> None:
    self.config = config
    self.metric = metric


def validate_config(config: MetricConfigInput) -> None:
  definition = get_metric_definition(config.name)
  if definition is None:
    raise MetricNotFoundError(f"Unknown metric: {config.name}")


def resolve_metric(config: MetricConfigInput, llm: DeepEvalBaseLLM) -> BaseMetric:
  validate_config(config)
  return build_builtin_metric(config, llm)


def resolve_metrics(
  configs: list[MetricConfigInput],
  llm: DeepEvalBaseLLM,
) -> list[ResolvedMetric]:
  resolved: list[ResolvedMetric] = []
  for config in configs:
    metric = resolve_metric(config, llm)
    resolved.append(ResolvedMetric(config=config, metric=metric))
  return resolved
