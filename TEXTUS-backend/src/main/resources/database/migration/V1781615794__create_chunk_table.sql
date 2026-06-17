CREATE TABLE IF NOT EXISTS books.chunk (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGSERIAL NOT NULL REFERENCES books.book(id),
    tokens_in INTEGER,
    tokens_out INTEGER,
    page INTEGER,
    title TEXT,
    summary TEXT,
    key TEXT
)