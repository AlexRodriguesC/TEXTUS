CREATE TABLE IF NOT EXISTS documents.topic
(
    id          BIGSERIAL PRIMARY KEY,
    page_id     BIGINT REFERENCES documents.page(id) ON DELETE CASCADE,
    node_id     BIGINT REFERENCES documents.lesson2(id) ON DELETE CASCADE,
    tokens_in   INTEGER,
    tokens_out  INTEGER,
    title       TEXT,
    summary     TEXT
);