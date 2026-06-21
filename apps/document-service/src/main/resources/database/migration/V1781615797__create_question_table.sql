CREATE TABLE IF NOT EXISTS documents.question (
    id BIGSERIAL PRIMARY KEY,
    chunk_id BIGSERIAL NOT NULL REFERENCES documents.chunk(id),
    question TEXT
)
