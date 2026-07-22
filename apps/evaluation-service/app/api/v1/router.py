"""API v1 router aggregating all controllers."""

from fastapi import APIRouter

from app.api.v1.controllers.evaluations_controller import router as evaluations_router
from app.api.v1.controllers.metrics_controller import router as metrics_router

api_router = APIRouter()
api_router.include_router(evaluations_router)
api_router.include_router(metrics_router)
