from logging import Logger

from backend.huey_app import huey
from backend.service.pipeline.chunk_extraction_step import extract_chunks
from backend.service.pipeline.cleanup_step import cleanup
from backend.service.pipeline.index_extraction_step import extract_index
from backend.service.pipeline.ocr_step import ocr_if_needed
from backend.service.pipeline.summary_extraction_step import extract_summaries

log = Logger(__name__)


def schedule_pipeline_for(book_id: str, job_id: str):
    log.info(f"Scheduling processing pipeline for book {book_id} and job {job_id}")

    pipeline = (
        ocr_if_needed.s(book_id, job_id)
        .then(extract_index.s(book_id, job_id))
        .then(extract_chunks.s(book_id, job_id))
        .then(extract_summaries.s(book_id, job_id))
        .then(cleanup.s(book_id, job_id))
    )

    huey.enqueue(pipeline)
