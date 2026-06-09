CREATE TABLE IF NOT EXISTS documents.chunk_key (
    id BIGSERIAL PRIMARY KEY,
    chunk_id BIGSERIAL NOT NULL REFERENCES documents.chunk(id),
    key_id BIGSERIAL NOT NULL REFERENCES documents.key(id)
)
