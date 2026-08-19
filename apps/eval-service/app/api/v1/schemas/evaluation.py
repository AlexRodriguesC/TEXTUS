"""Pydantic schemas for evaluation API requests and responses."""

from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, Field


class MetricConfig(BaseModel):
  """Configuration for a single metric to run during evaluation."""

  name: str
  threshold: float | None = None


class CreateEvaluationRequest(BaseModel):
  """Request body for POST /api/v1/evaluations."""

  content_type: str
  input: str
  actual_output: str
  expected_output: str | None = None
  retrieval_context: list[str] | None = None
  metrics: list[MetricConfig] = Field(min_length=1)


class MetricResultResponse(BaseModel):
  """Result of a single metric evaluation."""

  metric_name: str
  score: float
  reason: str
  success: bool
  error: str | None = None


class EvaluationResponse(BaseModel):
  """Response body for a single evaluation."""

  id: UUID
  content_type: str
  created_at: datetime
  results: list[MetricResultResponse]
