CREATE TABLE IF NOT EXISTS books.chunk (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGSERIAL NOT NULL REFERENCES books.book(id),
    page INTEGER,
    title TEXT,
    summary TEXT,
    key TEXT
)