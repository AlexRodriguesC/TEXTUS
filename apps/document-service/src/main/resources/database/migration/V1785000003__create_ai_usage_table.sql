CREATE TABLE IF NOT EXISTS documents.ai_usage
(
    id          BIGSERIAL PRIMARY KEY,
    entity_type TEXT        NOT NULL,
    entity_id   BIGINT      NOT NULL,
    operation   TEXT        NOT NULL,
    tokens_in   INTEGER     NOT NULL,
    tokens_out  INTEGER     NOT NULL,
    provider    TEXT        NOT NULL,
    model       TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_usage_entity ON documents.ai_usage (entity_type, entity_id);
