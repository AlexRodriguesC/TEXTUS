"""Application configuration loaded from environment variables."""

import re
from functools import lru_cache
from urllib.parse import quote_plus

from pydantic import Field, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


def jdbc_to_asyncpg_url(jdbc_url: str, username: str, password: str) -> str:
  """Convert a Spring JDBC URL to an async SQLAlchemy asyncpg URL."""
  match = re.match(r"^jdbc:postgresql://([^/]+)/([^?]+)", jdbc_url)
  if not match:
    raise ValueError(f"Unsupported JDBC URL format: {jdbc_url}")

  host_port = match.group(1)
  database = match.group(2)
  encoded_user = quote_plus(username)
  encoded_password = quote_plus(password)
  return (
    f"postgresql+asyncpg://{encoded_user}:{encoded_password}@{host_port}/{database}"
  )


class Settings(BaseSettings):
  """Runtime settings for the evaluation service."""

  model_config = SettingsConfigDict(
    env_file=".env",
    env_file_encoding="utf-8",
    extra="ignore",
    populate_by_name=True,
  )

  evaluation_database_url: str | None = Field(default=None, alias="EVALUATION_DATABASE_URL")
  spring_datasource_url: str | None = Field(default=None, alias="SPRING_DATASOURCE_URL")
  database_username: str = Field(default="appuser", alias="DATABASE_USERNAME")
  database_password: str = Field(default="appsecret", alias="DATABASE_PASSWORD")
  db_schema: str = Field(default="evaluations", alias="EVALUATION_DB_SCHEMA")
  openai_api_key: str | None = Field(default=None, alias="OPENAI_API_KEY")
  openai_base_url: str | None = Field(default=None, alias="OPENAI_BASE_URL")
  default_llm_model: str = Field(
    default="claude-haiku-4-5-free",
    alias="EVALUATION_LLM_MODEL",
  )

  @property
  def database_url(self) -> str:
    if self.evaluation_database_url:
      return self.evaluation_database_url
    if self.spring_datasource_url:
      return jdbc_to_asyncpg_url(
        self.spring_datasource_url,
        self.database_username,
        self.database_password,
      )
    return (
      f"postgresql+asyncpg://{quote_plus(self.database_username)}:"
      f"{quote_plus(self.database_password)}@localhost:5432/documents"
    )

  @field_validator("db_schema")
  @classmethod
  def validate_schema_name(cls, value: str) -> str:
    if not re.fullmatch(r"[a-zA-Z_][a-zA-Z0-9_]*", value):
      raise ValueError("db_schema must be a valid PostgreSQL identifier")
    return value


@lru_cache
def get_settings() -> Settings:
  return Settings()
