"""Business logic for running evaluations and persisting results."""

import asyncio
import logging
from uuid import UUID

from deepeval.metrics.base_metric import BaseMetric
from deepeval.test_case import LLMTestCase

from app.core.config import Settings
from app.core.exceptions import EvaluationNotFoundError
from app.models.domain import (
  EvaluationCreate,
  EvaluationCreateInput,
  EvaluationPage,
  EvaluationRecord,
  MetricDefinition,
  MetricResultRecord,
)
from app.repositories.evaluation_repository import EvaluationRepository
from app.services.llm.model_factory import get_evaluation_llm
from app.services.llm.retry import MAX_ATTEMPTS, is_retryable_llm_error, measure_with_retry
from app.services.metrics.factory import ResolvedMetric, resolve_metrics
from app.services.metrics.registry import list_metric_definitions

logger = logging.getLogger(__name__)


async def _run_single_metric(
        metric_name: str,
  metric: BaseMetric,
  test_case: LLMTestCase,
) -> MetricResultRecord:
  try:
    await measure_with_retry(
      lambda: metric.a_measure(test_case),
      metric_name=metric_name,
    )
    return MetricResultRecord(
      metric_name=metric_name,
      score=float(metric.score),
      reason=str(metric.reason or ""),
      success=bool(metric.success),
      error=None,
    )
  except Exception as exc:
    if is_retryable_llm_error(exc):
      logger.error(
        "Metric evaluation failed after %s attempts",
        MAX_ATTEMPTS,
        extra={"metric_name": metric_name, "error": str(exc)},
        exc_info=exc,
      )
    return MetricResultRecord(
      metric_name=metric_name,
      score=0.0,
      reason="",
      success=False,
      error=str(exc),
    )


class EvaluationService:
  """Orchestrates metric execution and evaluation persistence."""

  def __init__(self, repository: EvaluationRepository, settings: Settings) -> None:
    self._repository = repository
    self._settings = settings
    self._llm = get_evaluation_llm(settings)

  async def create_evaluation(self, request: EvaluationCreateInput) -> EvaluationRecord:
    resolved_metrics = resolve_metrics(request.metrics, self._llm)
    test_case = self._build_test_case(request)
    results = await self._run_metrics_concurrently(resolved_metrics, test_case)

    evaluation_create = EvaluationCreate(
      content_type=request.content_type,
      input=request.input,
      actual_output=request.actual_output,
      expected_output=request.expected_output,
      retrieval_context=request.retrieval_context,
    )
    return await self._repository.create(evaluation_create, results)

  async def get_evaluation(self, evaluation_id: UUID) -> EvaluationRecord:
    record = await self._repository.get_by_id(evaluation_id)
    if record is None:
      raise EvaluationNotFoundError(f"Evaluation not found: {evaluation_id}")
    return record

  async def list_evaluations(
    self,
    content_type: str | None,
    limit: int,
    offset: int,
  ) -> EvaluationPage:
    return await self._repository.list(
      content_type=content_type,
      limit=limit,
      offset=offset,
    )

  def list_available_metrics(self) -> list[MetricDefinition]:
    return list_metric_definitions()

  @staticmethod
  def _build_test_case(request: EvaluationCreateInput) -> LLMTestCase:
    return LLMTestCase(
      input=request.input,
      actual_output=request.actual_output,
      expected_output=request.expected_output,
      retrieval_context=request.retrieval_context,
      context=[request.input] if request.input else None,
    )

  async def _run_metrics_concurrently(
    self,
    resolved_metrics: list[ResolvedMetric],
    test_case: LLMTestCase,
  ) -> list[MetricResultRecord]:
    tasks = [
      _run_single_metric(resolved.config.name, resolved.metric, test_case)
      for resolved in resolved_metrics
    ]
    return list(await asyncio.gather(*tasks))
