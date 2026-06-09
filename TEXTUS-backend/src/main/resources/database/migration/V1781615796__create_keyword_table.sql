CREATE TABLE IF NOT EXISTS documents.keyword (
    id BIGSERIAL PRIMARY KEY,
    chunk_id BIGSERIAL NOT NULL REFERENCES documents.chunk(id),
    keyword TEXT
)
