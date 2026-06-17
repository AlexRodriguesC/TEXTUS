CREATE TABLE IF NOT EXISTS books.key (
    id BIGSERIAL PRIMARY KEY,
    chunk_id BIGSERIAL NOT NULL REFERENCES books.chunk(id),
    key TEXT,
    embedding_model TEXT,
    embedding vector(1536)
)
