from http import HTTPStatus

from fastapi import FastAPI

import backend
from backend.containers import Container
from backend.controller.PDFController import book_api


def create_app() -> FastAPI:
    container = Container()
    container.wire(modules=[backend.controller.PDFController])

    application = FastAPI(
        title="TEXTUS",
        description="REST API for TEXTUS",
        version="0.0.1",
        openapi_url="/openapi.json",
        docs_url="/docs",  # Swagger UI
        redoc_url="/redoc",
    )

    application.container = container
    application.include_router(book_api)

    return application


app = create_app()


@app.get("/health")
def health_check():
    return {"status": HTTPStatus.OK}
