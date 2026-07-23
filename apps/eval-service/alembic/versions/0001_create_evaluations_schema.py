"""Create evaluations schema and tables."""

from typing import Sequence, Union

import sqlalchemy as sa
from alembic import op
from sqlalchemy.dialects import postgresql

revision: str = "0001"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
  op.execute("CREATE SCHEMA IF NOT EXISTS evaluations")

  op.create_table(
    "evaluation",
    sa.Column("id", postgresql.UUID(as_uuid=True), primary_key=True, nullable=False),
    sa.Column("content_type", sa.String(length=255), nullable=False),
    sa.Column("input", sa.Text(), nullable=False),
    sa.Column("actual_output", sa.Text(), nullable=False),
    sa.Column("expected_output", sa.Text(), nullable=True),
    sa.Column("retrieval_context", postgresql.JSONB(astext_type=sa.Text()), nullable=True),
    sa.Column(
      "created_at",
      sa.DateTime(timezone=True),
      server_default=sa.text("now()"),
      nullable=False,
    ),
    schema="evaluations",
  )
  op.create_index(
    "ix_evaluations_evaluation_content_type",
    "evaluation",
    ["content_type"],
    schema="evaluations",
  )

  op.create_table(
    "metric_result",
    sa.Column("id", postgresql.UUID(as_uuid=True), primary_key=True, nullable=False),
    sa.Column("evaluation_id", postgresql.UUID(as_uuid=True), nullable=False),
    sa.Column("metric_name", sa.String(length=255), nullable=False),
    sa.Column("score", sa.Float(), nullable=False),
    sa.Column("reason", sa.Text(), nullable=False),
    sa.Column("success", sa.Boolean(), nullable=False),
    sa.Column("error", sa.Text(), nullable=True),
    sa.ForeignKeyConstraint(
      ["evaluation_id"],
      ["evaluations.evaluation.id"],
      ondelete="CASCADE",
    ),
    schema="evaluations",
  )
  op.create_index(
    "ix_evaluations_metric_result_evaluation_id",
    "metric_result",
    ["evaluation_id"],
    schema="evaluations",
  )


def downgrade() -> None:
  op.drop_index(
    "ix_evaluations_metric_result_evaluation_id",
    table_name="metric_result",
    schema="evaluations",
  )
  op.drop_table("metric_result", schema="evaluations")
  op.drop_index(
    "ix_evaluations_evaluation_content_type",
    table_name="evaluation",
    schema="evaluations",
  )
  op.drop_table("evaluation", schema="evaluations")
  op.execute("DROP SCHEMA IF EXISTS evaluations CASCADE")
