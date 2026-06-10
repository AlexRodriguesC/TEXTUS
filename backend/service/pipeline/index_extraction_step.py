from logging import Logger

from backend.huey_app import huey

log = Logger(__name__)


@huey.task(retries=2, retry_delay=60, name="index_extraction")
def extract_index(prev_result, book_id: str, job_id: str):
    log.info("Running extract index step")
    return ""
