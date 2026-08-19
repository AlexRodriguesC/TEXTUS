"""Map domain exceptions to HTTP error responses."""

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.core.exceptions import (
  EvaluationNotFoundError,
  EvaluationServiceError,
  InvalidMetricConfigError,
  MetricNotFoundError,
)


def register_exception_handlers(app: FastAPI) -> None:
  @app.exception_handler(MetricNotFoundError)
  async def metric_not_found_handler(
    _request: Request,
    exc: MetricNotFoundError,
  ) -> JSONResponse:
    return JSONResponse(status_code=400, content={"detail": str(exc)})

  @app.exception_handler(InvalidMetricConfigError)
  async def invalid_metric_config_handler(
    _request: Request,
    exc: InvalidMetricConfigError,
  ) -> JSONResponse:
    return JSONResponse(status_code=422, content={"detail": str(exc)})

  @app.exception_handler(EvaluationNotFoundError)
  async def evaluation_not_found_handler(
    _request: Request,
    exc: EvaluationNotFoundError,
  ) -> JSONResponse:
    return JSONResponse(status_code=404, content={"detail": str(exc)})

  @app.exception_handler(EvaluationServiceError)
  async def evaluation_service_error_handler(
    _request: Request,
    exc: EvaluationServiceError,
  ) -> JSONResponse:
    return JSONResponse(status_code=500, content={"detail": str(exc)})
