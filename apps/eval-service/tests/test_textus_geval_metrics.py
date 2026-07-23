"""Tests for TEXTUS GEval metric presets."""

import pytest
from deepeval.metrics import GEval
from deepeval.models import GPTModel
from deepeval.test_case import SingleTurnParams

from app.core.exceptions import MetricNotFoundError
from app.models.domain import MetricConfigInput
from app.services.metrics.factory import resolve_metric, validate_config
from app.services.metrics.registry import list_metric_definitions
from app.services.metrics.textus_geval import TEXTUS_GEVAL_PRESETS

TEXTUS_METRIC_NAMES = frozenset(TEXTUS_GEVAL_PRESETS)


@pytest.fixture
def stub_llm() -> GPTModel:
  return GPTModel(model="stub-model", api_key="test-key", base_url="https://stub/v1")


def test_textus_metrics_are_registered() -> None:
  registered_names = {definition.name for definition in list_metric_definitions()}
  assert TEXTUS_METRIC_NAMES <= registered_names


@pytest.mark.parametrize("metric_name", sorted(TEXTUS_METRIC_NAMES))
def test_validate_config_accepts_preset_without_criteria(metric_name: str) -> None:
  validate_config(MetricConfigInput(name=metric_name))


def test_validate_config_rejects_unknown_metric() -> None:
  with pytest.raises(MetricNotFoundError, match="Unknown metric"):
    validate_config(MetricConfigInput(name="not_a_metric"))


@pytest.mark.parametrize("metric_name", sorted(TEXTUS_METRIC_NAMES))
def test_resolve_metric_builds_geval_with_preset(
  metric_name: str,
  stub_llm: GPTModel,
) -> None:
  preset = TEXTUS_GEVAL_PRESETS[metric_name]
  metric = resolve_metric(MetricConfigInput(name=metric_name), stub_llm)

  assert isinstance(metric, GEval)
  assert metric.name == metric_name
  assert metric.criteria == preset.criteria
  assert metric.evaluation_params == preset.evaluation_params
  assert metric.threshold == preset.default_threshold


def test_resolve_metric_uses_custom_threshold(stub_llm: GPTModel) -> None:
  metric = resolve_metric(
    MetricConfigInput(name="topic_coverage", threshold=0.9),
    stub_llm,
  )

  assert metric.threshold == 0.9


def test_topic_granularity_uses_actual_output_only(stub_llm: GPTModel) -> None:
  metric = resolve_metric(MetricConfigInput(name="topic_granularity"), stub_llm)

  assert metric.evaluation_params == [SingleTurnParams.ACTUAL_OUTPUT]
