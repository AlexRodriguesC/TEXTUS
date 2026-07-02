CREATE TABLE IF NOT EXISTS documents.mind_map
(
    id     BIGSERIAL PRIMARY KEY,
    job_id BIGSERIAL REFERENCES documents.batch_job_instance(job_instance_id)
)
