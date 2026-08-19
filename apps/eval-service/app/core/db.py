"""Async SQLAlchemy engine and session management."""

from collections.abc import AsyncGenerator

from sqlalchemy.ext.asyncio import (
  AsyncEngine,
  AsyncSession,
  async_sessionmaker,
  create_async_engine,
)

from app.core.config import Settings, get_settings

_engine: AsyncEngine | None = None
_session_factory: async_sessionmaker[AsyncSession] | None = None


def create_engine(settings: Settings | None = None) -> AsyncEngine:
  resolved_settings = settings or get_settings()
  return create_async_engine(
    resolved_settings.database_url,
    echo=False,
    pool_pre_ping=True,
  )


def get_engine() -> AsyncEngine:
  global _engine
  if _engine is None:
    _engine = create_engine()
  return _engine


def get_session_factory() -> async_sessionmaker[AsyncSession]:
  global _session_factory
  if _session_factory is None:
    _session_factory = async_sessionmaker(
      bind=get_engine(),
      class_=AsyncSession,
      expire_on_commit=False,
    )
  return _session_factory


async def get_session() -> AsyncGenerator[AsyncSession]:
  session_factory = get_session_factory()
  async with session_factory() as session:
    yield session


async def dispose_engine() -> None:
  global _engine, _session_factory
  if _engine is not None:
    await _engine.dispose()
    _engine = None
    _session_factory = None
