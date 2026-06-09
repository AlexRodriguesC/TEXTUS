from typing import Annotated
from fastapi import APIRouter, UploadFile, File, HTTPException, Depends
from dependency_injector.wiring import inject, Provide
from http import HTTPStatus

from backend.containers import Container
from backend.config import ApplicationSettings
from backend.service import PDFService

book_api = APIRouter(prefix="/api/v1")

@book_api.post("/books", status_code=HTTPStatus.CREATED)
@inject
async def upload_book(
        file: Annotated[UploadFile, File(description="Book file")],
        settings: Annotated[
            ApplicationSettings,
            Depends(Provide[Container.application_settings]),
        ],
        pdf_service: Annotated[
            PDFService,
            Depends(Provide[Container.pdf_service]),
        ],
):
    if file.content_type != "application/pdf":
        raise HTTPException(
            status_code=HTTPStatus.UNSUPPORTED_MEDIA_TYPE,
            detail="Only PDF files are supported!",
        )

    max_size_mb = settings.max_upload_size_mb
    max_size_bytes = max_size_mb * 1024 * 1024
    file_size = file.size

    if file_size > max_size_bytes:
        raise HTTPException(
            status_code=HTTPStatus.BAD_REQUEST,
            detail=f"File of size {file_size} exceeds max allowed size of {max_size_mb} MB!",
        )

    return await pdf_service.handle_upload(file)
