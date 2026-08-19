"""HTTP controller for metric discovery."""

from fastapi import APIRouter, status

from app.api.v1.schemas.mappers import metric_definition_to_response
from app.api.v1.schemas.metrics import MetricDefinitionResponse
from app.core.deps import EvaluationServiceDep

router = APIRouter(prefix="/metrics", tags=["metrics"])


@router.get(
  "",
  response_model=list[MetricDefinitionResponse],
  status_code=status.HTTP_200_OK,
)
async def list_metrics(service: EvaluationServiceDep) -> list[MetricDefinitionResponse]:
  definitions = service.list_available_metrics()
  return [metric_definition_to_response(definition) for definition in definitions]
