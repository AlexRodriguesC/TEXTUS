CREATE TABLE IF NOT EXISTS documents.document
(
    id       BIGSERIAL PRIMARY KEY,
    name     TEXT NOT NULL,
    job_id   BIGSERIAL REFERENCES documents.batch_job_instance(job_instance_id),
    space_id BIGSERIAL REFERENCES documents.space(id) ON DELETE CASCADE
);
