"""Pydantic schemas for paginated evaluation list responses."""

from pydantic import BaseModel, Field

from app.api.v1.schemas.evaluation import EvaluationResponse


class PaginatedEvaluationListResponse(BaseModel):
  """Paginated list of evaluations."""

  items: list[EvaluationResponse] = Field(default_factory=list)
  total: int
  limit: int
  offset: int
