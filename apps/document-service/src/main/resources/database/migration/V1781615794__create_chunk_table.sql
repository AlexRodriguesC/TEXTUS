CREATE TABLE IF NOT EXISTS documents.chunk (
    id BIGSERIAL PRIMARY KEY,
    space_id BIGSERIAL REFERENCES documents.space(id) ON DELETE CASCADE,
    document_id BIGSERIAL REFERENCES documents.document(id),
    tokens_in INTEGER,
    tokens_out INTEGER,
    title TEXT,
    summary TEXT,
    key TEXT
);

CREATE TABLE IF NOT EXISTS documents.page (
    id BIGSERIAL PRIMARY KEY,
    space_id BIGSERIAL REFERENCES documents.space(id) ON DELETE CASCADE,
    document_id BIGSERIAL REFERENCES documents.document(id) ON DELETE CASCADE,
    tokens_in INTEGER,
    tokens_out INTEGER,
    title TEXT,
    summary TEXT
);
