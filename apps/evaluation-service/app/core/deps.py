"""FastAPI dependency injection wiring for repositories and services."""

from typing import Annotated

from fastapi import Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import Settings, get_settings
from app.core.db import get_session
from app.repositories.evaluation_repository import EvaluationRepository
from app.services.evaluation_service import EvaluationService


def get_evaluation_repository(
  session: Annotated[AsyncSession, Depends(get_session)],
) -> EvaluationRepository:
  return EvaluationRepository(session)


def get_evaluation_service(
  repository: Annotated[EvaluationRepository, Depends(get_evaluation_repository)],
  settings: Annotated[Settings, Depends(get_settings)],
) -> EvaluationService:
  return EvaluationService(repository=repository, settings=settings)


AsyncSessionDep = Annotated[AsyncSession, Depends(get_session)]
EvaluationRepositoryDep = Annotated[EvaluationRepository, Depends(get_evaluation_repository)]
EvaluationServiceDep = Annotated[EvaluationService, Depends(get_evaluation_service)]
SettingsDep = Annotated[Settings, Depends(get_settings)]
