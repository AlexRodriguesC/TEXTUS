from logging import Logger
from pathlib import Path

from pymupdf import pymupdf

from backend.huey_app import huey

log = Logger(__name__)


@huey.task(retries=2, retry_delay=60, name="ocr_if_needed")
def ocr_if_needed(book_id: str, job_id: str):
    log.info(f"Running OCR step for book {book_id} and job {job_id}")

    books_dir = Path(__file__).parent.parent / "storage" / "books"
    matches = list(books_dir.glob(f"{book_id}.*"))

    if not matches:
        raise FileNotFoundError(f"No book found with id '{book_id}' in {books_dir}")

    book_path = matches[0]
    book = pymupdf.open(book_path)
    print(book.name)
