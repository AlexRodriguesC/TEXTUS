CREATE TABLE IF NOT EXISTS books.index_entry_summary
(
    id BIGSERIAL PRIMARY KEY,
    book_id BIGSERIAL NOT NULL REFERENCES books.book(id),
    index_id BIGSERIAL NOT NULL REFERENCES books.index(id),
    content TEXT NOT NULL
)