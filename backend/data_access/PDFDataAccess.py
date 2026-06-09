from pydantic import BaseModel
from redis import Redis

from backend.domain.JobStatus import JobStatus


class BookStatus(BaseModel):
    job_id: str
    job_status: JobStatus


class PDFDataAccess:
    def __init__(self, redis: Redis):
        self.redis = redis

    async def add_book(self, book_id: str, job_id: str):
        book_status = BookStatus(job_id=job_id, job_status=JobStatus.ENQUEUED)

        self.redis.set(
            name=f"book:{book_id}",
            value=book_status.model_dump_json(),
        )

    async def get_job_status_for_book(self, book_id: str) -> BookStatus | None:
        raw = self.redis.get(name=book_id)
        if raw is None:
            return None

        if isinstance(raw, bytes):
            raw = raw.decode("utf-8")

        return BookStatus.model_validate_json(raw)


class BookNotFoundError(Exception):
    pass


async def update_job_status(self, book_id: str, status: JobStatus) -> None:
    exists = await self.redis.exists(book_id)
    if not exists:
        raise BookNotFoundError(f"Book {book_id} not found")

    raw = await self.redis.get(book_id)
    book_status = BookStatus.model_validate_json(raw)
    book_status.job_status = status

    await self.redis.set(book_id, book_status.model_dump_json())
