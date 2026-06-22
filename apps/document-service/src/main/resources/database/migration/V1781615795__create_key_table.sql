CREATE TABLE IF NOT EXISTS documents.key (
    id BIGSERIAL PRIMARY KEY,
    chunk_id BIGSERIAL NOT NULL REFERENCES documents.chunk(id),
    key TEXT
)
