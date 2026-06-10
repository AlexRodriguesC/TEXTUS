import logging
from uuid import uuid4

from fastapi import UploadFile

from backend.data_access.PDFDataAccess import PDFDataAccess
from backend.data_access.PDFFileStorage import PDFFileStorage
from backend.service.PDFJobScheduler import schedule_pipeline_for

logger = logging.getLogger(__name__)


class PDFService:
    def __init__(self, pdf_data_access: PDFDataAccess, pdf_file_storage: PDFFileStorage):
        self.pdf_data_access = pdf_data_access
        self.pdf_file_storage = pdf_file_storage

    async def handle_upload(self, file: UploadFile):
        book_id = str(uuid4())
        job_id = str(uuid4())

        await self.pdf_file_storage.save_book_file(book_id, file)
        await self.pdf_data_access.add_book(book_id=book_id, job_id=job_id)

        schedule_pipeline_for(book_id, job_id)

        return {
            "book_id": book_id,
            "job_id": job_id,
        }
