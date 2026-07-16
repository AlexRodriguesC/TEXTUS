CREATE TABLE IF NOT EXISTS documents.attempt
(
    id               BIGSERIAL PRIMARY KEY,
    exercise_id      BIGINT REFERENCES documents.exercise (id) ON DELETE CASCADE,
    node_id        BIGINT REFERENCES documents.node (id) ON DELETE CASCADE,
    user_id          TEXT REFERENCES users.user (username) ON DELETE CASCADE,

    responses        JSONB NOT NULL,                -- raw submitted answer payload, family-specific shape
    is_correct       BOOLEAN NOT NULL,
    prior_mastery    NUMERIC(4,3),
    posterior_mastery NUMERIC(4,3)
);