"""Tests for DeepEval built-in metrics and registry."""

import pytest
from deepeval.metrics import AnswerRelevancyMetric, HallucinationMetric
from deepeval.models import GPTModel

from app.models.domain import EvaluationCreateInput, MetricConfigInput
from app.services.evaluation_service import EvaluationService
from app.services.metrics.factory import resolve_metric
from app.services.metrics.registry import METRIC_REGISTRY, list_metric_definitions


@pytest.fixture
def stub_llm() -> GPTModel:
  return GPTModel(model="stub-model", api_key="test-key", base_url="https://stub/v1")


def test_registry_exposes_exactly_five_metrics() -> None:
  definitions = list_metric_definitions()
  assert len(definitions) == 5
  assert set(METRIC_REGISTRY) == {
    "hallucination",
    "topic_coverage",
    "topic_granularity",
    "question_relevance",
    "answer_relevancy",
  }


def test_resolve_hallucination_metric(stub_llm: GPTModel) -> None:
  metric = resolve_metric(MetricConfigInput(name="hallucination"), stub_llm)

  assert isinstance(metric, HallucinationMetric)
  assert metric.threshold == 0.5


def test_resolve_answer_relevancy_metric(stub_llm: GPTModel) -> None:
  metric = resolve_metric(MetricConfigInput(name="answer_relevancy"), stub_llm)

  assert isinstance(metric, AnswerRelevancyMetric)
  assert metric.threshold == 0.5


def test_build_test_case_sets_context_from_input() -> None:
  request = EvaluationCreateInput(
    content_type="summary",
    input="source text",
    actual_output="summary text",
    metrics=[MetricConfigInput(name="hallucination")],
  )

  test_case = EvaluationService._build_test_case(request)

  assert test_case.context == ["source text"]
  assert test_case.input == "source text"
  assert test_case.actual_output == "summary text"
