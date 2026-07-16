CREATE TABLE IF NOT EXISTS documents.exercise
(
    id      BIGSERIAL PRIMARY KEY,
    node_id BIGINT REFERENCES documents.lesson2 (id) ON DELETE CASCADE,
    topic_id     BIGINT NOT NULL REFERENCES documents.topic (id) ON DELETE CASCADE,
    family  TEXT NOT NULL CHECK (family IN ('question', 'pair', 'group', 'list')),
    index   INTEGER NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE
);