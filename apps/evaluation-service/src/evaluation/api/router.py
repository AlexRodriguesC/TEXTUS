from fastapi import APIRouter
from evaluation.api.routes import evaluation

api_router = APIRouter()
api_router.include_router(evaluation.router)