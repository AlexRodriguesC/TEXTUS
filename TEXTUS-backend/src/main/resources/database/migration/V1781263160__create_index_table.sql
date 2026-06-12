CREATE TABLE IF NOT EXISTS books.book_section
(
    id      BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL REFERENCES books.book (id),
    title   TEXT   NOT NULL,
    content TEXT   NOT NULL
)