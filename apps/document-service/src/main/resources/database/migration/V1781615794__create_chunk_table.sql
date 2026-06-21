CREATE TABLE IF NOT EXISTS documents.chunk (
    id BIGSERIAL PRIMARY KEY,
    tokens_in INTEGER,
    tokens_out INTEGER,
    title TEXT,
    summary TEXT,
    key TEXT
)