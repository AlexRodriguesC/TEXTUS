"""Repository for evaluation persistence using async SQLAlchemy."""

from uuid import UUID

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.domain import (
  EvaluationCreate,
  EvaluationListItem,
  EvaluationPage,
  EvaluationRecord,
  MetricResultRecord,
)
from app.models.orm import EvaluationORM, MetricResultORM


class EvaluationRepository:
  """Data access layer for evaluations and metric results."""

  def __init__(self, session: AsyncSession) -> None:
    self._session = session

  async def create(
    self,
    evaluation: EvaluationCreate,
    results: list[MetricResultRecord],
  ) -> EvaluationRecord:
    evaluation_orm = EvaluationORM(
      content_type=evaluation.content_type,
      input=evaluation.input,
      actual_output=evaluation.actual_output,
      expected_output=evaluation.expected_output,
      retrieval_context=evaluation.retrieval_context,
    )
    evaluation_orm.metric_results = [
      MetricResultORM(
        metric_name=result.metric_name,
        score=result.score,
        reason=result.reason,
        success=result.success,
        error=result.error,
      )
      for result in results
    ]

    self._session.add(evaluation_orm)
    await self._session.commit()
    await self._session.refresh(evaluation_orm)
    return self._to_record(evaluation_orm)

  async def get_by_id(self, evaluation_id: UUID) -> EvaluationRecord | None:
    evaluation_orm = await self._session.get(EvaluationORM, evaluation_id)
    if evaluation_orm is None:
      return None
    return self._to_record(evaluation_orm)

  async def list(
    self,
    content_type: str | None,
    limit: int,
    offset: int,
  ) -> EvaluationPage:
    base_query = select(EvaluationORM)
    count_query = select(func.count()).select_from(EvaluationORM)

    if content_type is not None:
      base_query = base_query.where(EvaluationORM.content_type == content_type)
      count_query = count_query.where(EvaluationORM.content_type == content_type)

    total_result = await self._session.execute(count_query)
    total = int(total_result.scalar_one())

    result = await self._session.execute(
      base_query.order_by(EvaluationORM.created_at.desc()).limit(limit).offset(offset)
    )
    evaluations = result.scalars().unique().all()

    return EvaluationPage(
      items=[self._to_list_item(evaluation) for evaluation in evaluations],
      total=total,
      limit=limit,
      offset=offset,
    )

  @staticmethod
  def _to_metric_result_record(result_orm: MetricResultORM) -> MetricResultRecord:
    return MetricResultRecord(
      metric_name=result_orm.metric_name,
      score=result_orm.score,
      reason=result_orm.reason,
      success=result_orm.success,
      error=result_orm.error,
    )

  def _to_record(self, evaluation_orm: EvaluationORM) -> EvaluationRecord:
    return EvaluationRecord(
      id=evaluation_orm.id,
      content_type=evaluation_orm.content_type,
      input=evaluation_orm.input,
      actual_output=evaluation_orm.actual_output,
      expected_output=evaluation_orm.expected_output,
      retrieval_context=evaluation_orm.retrieval_context,
      created_at=evaluation_orm.created_at,
      results=[
        self._to_metric_result_record(result)
        for result in evaluation_orm.metric_results
      ],
    )

  def _to_list_item(self, evaluation_orm: EvaluationORM) -> EvaluationListItem:
    return EvaluationListItem(
      id=evaluation_orm.id,
      content_type=evaluation_orm.content_type,
      created_at=evaluation_orm.created_at,
      results=[
        self._to_metric_result_record(result)
        for result in evaluation_orm.metric_results
      ],
    )
