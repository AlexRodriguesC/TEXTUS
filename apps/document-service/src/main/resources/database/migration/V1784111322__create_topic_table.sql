CREATE TABLE IF NOT EXISTS documents.topic
(
    id          BIGSERIAL PRIMARY KEY,
    page_id     BIGINT REFERENCES documents.page(id) ON DELETE CASCADE,
    node_id     BIGINT REFERENCES documents.node(id) ON DELETE CASCADE,
    tokens_in   INTEGER,
    tokens_out  INTEGER,
    title       TEXT,
    summary     TEXT
);