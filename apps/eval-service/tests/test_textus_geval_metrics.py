"""Tests for TEXTUS GEval metric presets."""

import pytest
from deepeval.metrics import GEval
from deepeval.metrics.g_eval import Rubric
from deepeval.models import GPTModel
from deepeval.test_case import SingleTurnParams

from app.core.exceptions import MetricNotFoundError
from app.models.domain import MetricConfigInput
from app.services.metrics.factory import resolve_metric, validate_config
from app.services.metrics.registry import list_metric_definitions
from app.services.metrics.textus_geval import TEXTUS_GEVAL_PRESETS

TEXTUS_METRIC_NAMES = frozenset(TEXTUS_GEVAL_PRESETS)

EXPECTED_RUBRICS: dict[str, list[tuple[tuple[int, int], str]]] = {
  "topic_coverage": [
    (
      (0, 0),
      "The topic list omits at least one major subject or theme present in the summary.",
    ),
    (
      (10, 10),
      "The topic list covers all distinct subjects/themes present in the summary, "
      "with no major topic omitted.",
    ),
  ],
  "topic_granularity": [
    (
      (0, 3),
      "Multiple topics are vague, redundant, or overlap significantly with other "
      "topics, making them unsuitable for generating a distinct question each.",
    ),
    (
      (4, 7),
      "Most topics are distinct and specific, but at least one topic is too vague, "
      "overlaps with another, or is not specific enough to generate a clear, unique "
      "question.",
    ),
    (
      (8, 10),
      "Every topic is distinct, non-redundant, and specific enough to generate a "
      "clear, meaningful question with no overlap with other topics.",
    ),
  ],
  "question_relevance": [
    (
      (0, 3),
      "The question is not answerable from the given topic and/or introduces "
      "significant unrelated content.",
    ),
    (
      (4, 7),
      "The question is mostly answerable from the topic but includes minor "
      "unrelated content or is only loosely relevant.",
    ),
    (
      (8, 10),
      "The question is directly answerable from and clearly relevant to the given "
      "topic, with no unrelated content introduced.",
    ),
  ],
}


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
def test_preset_defines_expected_rubric(metric_name: str) -> None:
  preset = TEXTUS_GEVAL_PRESETS[metric_name]
  expected = EXPECTED_RUBRICS[metric_name]

  assert preset.rubric is not None
  assert len(preset.rubric) == len(expected)
  for rubric, (score_range, expected_outcome) in zip(preset.rubric, expected, strict=True):
    assert isinstance(rubric, Rubric)
    assert rubric.score_range == score_range
    assert rubric.expected_outcome == expected_outcome


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
  assert metric.rubric == preset.rubric


def test_resolve_metric_uses_custom_threshold(stub_llm: GPTModel) -> None:
  metric = resolve_metric(
    MetricConfigInput(name="topic_coverage", threshold=0.9),
    stub_llm,
  )

  assert metric.threshold == 0.9


def test_topic_granularity_uses_actual_output_only(stub_llm: GPTModel) -> None:
  metric = resolve_metric(MetricConfigInput(name="topic_granularity"), stub_llm)

  assert metric.evaluation_params == [SingleTurnParams.ACTUAL_OUTPUT]
