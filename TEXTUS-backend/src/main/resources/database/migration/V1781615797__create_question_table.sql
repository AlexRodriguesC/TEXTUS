CREATE TABLE IF NOT EXISTS books.question (
    id BIGSERIAL PRIMARY KEY,
    chunk_id BIGSERIAL NOT NULL REFERENCES books.chunk(id),
    question TEXT
)
