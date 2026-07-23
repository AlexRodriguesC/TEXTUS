"""Pydantic schemas for the metrics discovery API."""

from pydantic import BaseModel, Field


class MetricDefinitionResponse(BaseModel):
  """Describes an available metric and its required parameters."""

  name: str
  description: str
  required_params: list[str] = Field(default_factory=list)
