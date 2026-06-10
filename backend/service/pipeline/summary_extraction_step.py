from logging import Logger

from backend.huey_app import huey

log = Logger(__name__)


@huey.task(retries=2, retry_delay=60, name="summary_extraction")
def extract_summaries(prev_result, book_id: str, job_id: str):
    log.info("Running summary step")
    return ""
