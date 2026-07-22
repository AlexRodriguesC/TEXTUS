"""SQLAlchemy ORM models mapped to the evaluations schema."""

import uuid
from datetime import datetime

from sqlalchemy import Boolean, DateTime, Float, ForeignKey, String, Text, func
from sqlalchemy.dialects.postgresql import JSONB, UUID
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column, relationship


class Base(DeclarativeBase):
  """Declarative base for all ORM models."""


class EvaluationORM(Base):
  """ORM model for evaluations.evaluation."""

  __tablename__ = "evaluation"
  __table_args__ = {"schema": "evaluations"}

  id: Mapped[uuid.UUID] = mapped_column(
    UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
  )
  content_type: Mapped[str] = mapped_column(String(255), nullable=False, index=True)
  input: Mapped[str] = mapped_column(Text, nullable=False)
  actual_output: Mapped[str] = mapped_column(Text, nullable=False)
  expected_output: Mapped[str | None] = mapped_column(Text, nullable=True)
  retrieval_context: Mapped[list[str] | None] = mapped_column(JSONB, nullable=True)
  created_at: Mapped[datetime] = mapped_column(
    DateTime(timezone=True), nullable=False, server_default=func.now()
  )

  metric_results: Mapped[list["MetricResultORM"]] = relationship(
    back_populates="evaluation",
    cascade="all, delete-orphan",
    lazy="selectin",
  )


class MetricResultORM(Base):
  """ORM model for evaluations.metric_result."""

  __tablename__ = "metric_result"
  __table_args__ = {"schema": "evaluations"}

  id: Mapped[uuid.UUID] = mapped_column(
    UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
  )
  evaluation_id: Mapped[uuid.UUID] = mapped_column(
    UUID(as_uuid=True),
    ForeignKey("evaluations.evaluation.id", ondelete="CASCADE"),
    nullable=False,
    index=True,
  )
  metric_name: Mapped[str] = mapped_column(String(255), nullable=False)
  score: Mapped[float] = mapped_column(Float, nullable=False)
  reason: Mapped[str] = mapped_column(Text, nullable=False)
  success: Mapped[bool] = mapped_column(Boolean, nullable=False)
  error: Mapped[str | None] = mapped_column(Text, nullable=True)

  evaluation: Mapped[EvaluationORM] = relationship(back_populates="metric_results")
