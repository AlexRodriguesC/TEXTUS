"""Mappers between API schemas and domain models."""

from app.api.v1.schemas.evaluation import (
  CreateEvaluationRequest,
  EvaluationResponse,
  MetricConfig,
  MetricResultResponse,
)
from app.api.v1.schemas.metrics import MetricDefinitionResponse
from app.api.v1.schemas.pagination import PaginatedEvaluationListResponse
from app.models.domain import (
  EvaluationCreateInput,
  EvaluationListItem,
  EvaluationPage,
  EvaluationRecord,
  MetricConfigInput,
  MetricDefinition,
  MetricResultRecord,
)


def metric_config_to_domain(config: MetricConfig) -> MetricConfigInput:
  return MetricConfigInput(
    name=config.name,
    threshold=config.threshold,
  )


def create_request_to_domain(request: CreateEvaluationRequest) -> EvaluationCreateInput:
  return EvaluationCreateInput(
    content_type=request.content_type,
    input=request.input,
    actual_output=request.actual_output,
    expected_output=request.expected_output,
    retrieval_context=request.retrieval_context,
    metrics=[metric_config_to_domain(metric) for metric in request.metrics],
  )


def metric_result_to_response(result: MetricResultRecord) -> MetricResultResponse:
  return MetricResultResponse(
    metric_name=result.metric_name,
    score=result.score,
    reason=result.reason,
    success=result.success,
    error=result.error,
  )


def evaluation_record_to_response(record: EvaluationRecord) -> EvaluationResponse:
  return EvaluationResponse(
    id=record.id,
    content_type=record.content_type,
    created_at=record.created_at,
    results=[metric_result_to_response(result) for result in record.results],
  )


def evaluation_list_item_to_response(item: EvaluationListItem) -> EvaluationResponse:
  return EvaluationResponse(
    id=item.id,
    content_type=item.content_type,
    created_at=item.created_at,
    results=[metric_result_to_response(result) for result in item.results],
  )


def evaluation_page_to_response(page: EvaluationPage) -> PaginatedEvaluationListResponse:
  return PaginatedEvaluationListResponse(
    items=[evaluation_list_item_to_response(item) for item in page.items],
    total=page.total,
    limit=page.limit,
    offset=page.offset,
  )


def metric_definition_to_response(definition: MetricDefinition) -> MetricDefinitionResponse:
  return MetricDefinitionResponse(
    name=definition.name,
    description=definition.description,
    required_params=definition.required_params,
  )
