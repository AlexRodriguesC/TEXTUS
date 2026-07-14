CREATE TABLE IF NOT EXISTS documents.topic
(
    id          BIGSERIAL PRIMARY KEY,
    page_id BIGSERIAL REFERENCES documents.page(id) ON DELETE CASCADE,
    chapter_id BIGSERIAL NULL,
    tokens_in   INTEGER,
    tokens_out  INTEGER,
    title       TEXT,
    summary     TEXT
);