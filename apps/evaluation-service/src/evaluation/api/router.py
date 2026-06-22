from fastapi import APIRouter
from src.evaluation.api.routes.evaluation import router

api_router = APIRouter()
api_router.include_router(router)