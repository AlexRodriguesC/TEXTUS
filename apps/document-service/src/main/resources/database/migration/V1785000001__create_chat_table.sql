CREATE TABLE IF NOT EXISTS documents.chat
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    space_id   BIGINT      NOT NULL REFERENCES documents.space (id) ON DELETE CASCADE,
    owner_id   TEXT        NOT NULL REFERENCES users.user (username),
    title      TEXT        NOT NULL DEFAULT 'New chat',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS chat_space_owner_idx ON documents.chat (space_id, owner_id);
