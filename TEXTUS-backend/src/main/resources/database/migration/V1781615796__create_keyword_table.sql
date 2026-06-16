CREATE TABLE IF NOT EXISTS books.keyword (
    id BIGSERIAL PRIMARY KEY,
    chunk_id BIGSERIAL NOT NULL REFERENCES books.chunk(id),
    keyword TEXT
)
