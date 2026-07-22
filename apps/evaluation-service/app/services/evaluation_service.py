"""Business logic for running evaluations and persisting results."""

import asyncio
import os
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
from app.services.metrics.factory import ResolvedMetric, resolve_metrics
from app.services.metrics.registry import list_metric_definitions


class EvaluationService:
  """Orchestrates metric execution and evaluation persistence."""

  def __init__(self, repository: EvaluationRepository, settings: Settings) -> None:
    self._repository = repository
    self._settings = settings
    self._configure_deepeval_environment()

  def _configure_deepeval_environment(self) -> None:
    if self._settings.openai_api_key:
      os.environ.setdefault("OPENAI_API_KEY", self._settings.openai_api_key)
    if self._settings.openai_base_url:
      os.environ.setdefault("OPENAI_BASE_URL", self._settings.openai_base_url)

  async def create_evaluation(self, request: EvaluationCreateInput) -> EvaluationRecord:
    resolved_metrics = resolve_metrics(request.metrics, self._settings)
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
    )

  async def _run_metrics_concurrently(
    self,
    resolved_metrics: list[ResolvedMetric],
    test_case: LLMTestCase,
  ) -> list[MetricResultRecord]:
    tasks = [
      self._run_single_metric(resolved.config.name, resolved.metric, test_case)
      for resolved in resolved_metrics
    ]
    return list(await asyncio.gather(*tasks))

  async def _run_single_metric(
    self,
    metric_name: str,
    metric: BaseMetric,
    test_case: LLMTestCase,
  ) -> MetricResultRecord:
    try:
      await metric.a_measure(test_case)
      return MetricResultRecord(
        metric_name=metric_name,
        score=float(metric.score),
        reason=str(metric.reason or ""),
        success=bool(metric.success),
        error=None,
      )
    except Exception as exc:
      return MetricResultRecord(
        metric_name=metric_name,
        score=0.0,
        reason="",
        success=False,
        error=str(exc),
      )
