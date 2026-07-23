"""FastAPI application entrypoint."""

from collections.abc import AsyncIterator
from contextlib import asynccontextmanager

from fastapi import FastAPI
from sqlalchemy import text

from app.api.v1.router import api_router
from app.core.db import dispose_engine, get_engine
from app.core.exception_handlers import register_exception_handlers


@asynccontextmanager
async def lifespan(_app: FastAPI) -> AsyncIterator[None]:
  engine = get_engine()
  async with engine.connect() as connection:
    await connection.execute(text("SELECT 1"))
  yield
  await dispose_engine()


app = FastAPI(
  title="TEXTUS Evaluation Service",
  version="0.1.0",
  lifespan=lifespan,
)
register_exception_handlers(app)
app.include_router(api_router, prefix="/api/v1")


@app.get("/health", tags=["health"])
async def health() -> dict[str, str]:
  return {"status": "ok"}
