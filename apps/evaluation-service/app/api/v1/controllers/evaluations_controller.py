"""HTTP controller for evaluation resources."""

from uuid import UUID

from fastapi import APIRouter, Query, status

from app.api.v1.schemas.evaluation import CreateEvaluationRequest, EvaluationResponse
from app.api.v1.schemas.mappers import (
  create_request_to_domain,
  evaluation_page_to_response,
  evaluation_record_to_response,
)
from app.api.v1.schemas.pagination import PaginatedEvaluationListResponse
from app.core.deps import EvaluationServiceDep

router = APIRouter(prefix="/evaluations", tags=["evaluations"])


@router.post(
  "",
  response_model=EvaluationResponse,
  status_code=status.HTTP_201_CREATED,
)
async def create_evaluation(
  request: CreateEvaluationRequest,
  service: EvaluationServiceDep,
) -> EvaluationResponse:
  record = await service.create_evaluation(create_request_to_domain(request))
  return evaluation_record_to_response(record)


@router.get(
  "/{evaluation_id}",
  response_model=EvaluationResponse,
  status_code=status.HTTP_200_OK,
)
async def get_evaluation(
  evaluation_id: UUID,
  service: EvaluationServiceDep,
) -> EvaluationResponse:
  record = await service.get_evaluation(evaluation_id)
  return evaluation_record_to_response(record)


@router.get(
  "",
  response_model=PaginatedEvaluationListResponse,
  status_code=status.HTTP_200_OK,
)
async def list_evaluations(
  service: EvaluationServiceDep,
  content_type: str | None = None,
  limit: int = Query(default=20, ge=1, le=100),
  offset: int = Query(default=0, ge=0),
) -> PaginatedEvaluationListResponse:
  page = await service.list_evaluations(
    content_type=content_type,
    limit=limit,
    offset=offset,
  )
  return evaluation_page_to_response(page)
