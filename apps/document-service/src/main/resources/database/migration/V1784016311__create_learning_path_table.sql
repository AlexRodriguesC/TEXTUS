CREATE TABLE IF NOT EXISTS documents.learning_path
(
    id          BIGSERIAL PRIMARY KEY,
    space_id    BIGINT REFERENCES documents.space(id) ON DELETE CASCADE,
    job_id BIGSERIAL REFERENCES documents.batch_job_instance(job_instance_id)
);
