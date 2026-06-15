CREATE TABLE IF NOT EXISTS books.index
(
    id BIGSERIAL PRIMARY KEY,
    book_id BIGSERIAL NOT NULL REFERENCES books.book (id),
    title TEXT,
    position INT,
    from_page INT,
    to_page INT
)