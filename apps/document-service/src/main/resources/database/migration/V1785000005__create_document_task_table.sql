CREATE TABLE IF NOT EXISTS documents.document_task
(
    id           TEXT PRIMARY KEY,
    document_id  BIGINT      NOT NULL REFERENCES documents.document (id) ON DELETE CASCADE,
    type         TEXT        NOT NULL,
    progress     INT         NOT NULL DEFAULT 0,
    max_progress INT         NOT NULL,
    variant      TEXT        NOT NULL DEFAULT 'DEFAULT',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (document_id, type)
);
