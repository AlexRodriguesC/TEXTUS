from backend.huey_app import huey

def schedule_pipeline_for(book_id: str, job_id: str):
    pipeline = extract_index.then(extract_chunks).then(extract_summaries).then(cleanup)
    huey.enqueue(pipeline)

@huey.task(retries=2, retry_delay=60, name="index_extraction")
def extract_index(book_id: str, job_id: str):
    return ""


@huey.task(retries=2, retry_delay=60, name="chunking")
def extract_chunks(book_id: str, job_id: str):
    return ""


@huey.task(retries=2, retry_delay=60, name="summary_extraction")
def extract_summaries(book_id: str, job_id: str):
    return ""


@huey.task(retries=2, retry_delay=60, name="cleanup")
def cleanup(book_id: str, job_id: str):
    return ""
