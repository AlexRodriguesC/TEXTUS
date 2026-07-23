"""Pydantic domain models exchanged between the service and repository layers.

These models are decoupled from HTTP API schemas and SQLAlchemy ORM entities.
"""

from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, Field


class MetricResultRecord(BaseModel):
  """Outcome of a single metric evaluation."""

  metric_name: str
  score: float
  reason: str
  success: bool
  error: str | None = None


class EvaluationCreate(BaseModel):
  """Input required to persist a new evaluation."""

  content_type: str
  input: str
  actual_output: str
  expected_output: str | None = None
  retrieval_context: list[str] | None = None


class EvaluationRecord(BaseModel):
  """A persisted evaluation with its metric results."""

  id: UUID
  content_type: str
  input: str
  actual_output: str
  expected_output: str | None = None
  retrieval_context: list[str] | None = None
  created_at: datetime
  results: list[MetricResultRecord] = Field(default_factory=list)


class EvaluationListItem(BaseModel):
  """Summary of an evaluation for list responses."""

  id: UUID
  content_type: str
  created_at: datetime
  results: list[MetricResultRecord] = Field(default_factory=list)


class EvaluationPage(BaseModel):
  """Paginated list of evaluations."""

  items: list[EvaluationListItem]
  total: int
  limit: int
  offset: int


class MetricConfigInput(BaseModel):
  """Metric configuration passed from the API into the service layer."""

  name: str
  threshold: float | None = None


class EvaluationCreateInput(BaseModel):
  """Full create-evaluation input including metrics to run."""

  content_type: str
  input: str
  actual_output: str
  expected_output: str | None = None
  retrieval_context: list[str] | None = None
  metrics: list[MetricConfigInput]


class MetricDefinition(BaseModel):
  """Metadata describing a registered metric."""

  name: str
  description: str
  required_params: list[str] = Field(default_factory=list)
