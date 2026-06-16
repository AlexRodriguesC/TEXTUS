CREATE TABLE IF NOT EXISTS books.chunk_key (
    id BIGSERIAL PRIMARY KEY,
    chunk_id BIGSERIAL NOT NULL REFERENCES books.chunk(id),
    key_id BIGSERIAL NOT NULL REFERENCES books.key(id)
)
