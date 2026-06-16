CREATE TABLE IF NOT EXISTS books.index_entry
(
    id BIGSERIAL PRIMARY KEY,
    index_id BIGSERIAL NOT NULL REFERENCES books.index(id),
    title TEXT,
    position INT,
    from_page INT,
    to_page INT
)