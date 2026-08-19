CREATE TABLE IF NOT EXISTS documents.topic_type
(
    topic_id BIGINT NOT NULL REFERENCES documents.topic (id) ON DELETE CASCADE,
    type     TEXT   NOT NULL,
    PRIMARY KEY (topic_id, type),
    CONSTRAINT topic_type_valid CHECK (type IN ('LIST', 'GROUP', 'PAIR', 'QUESTION'))
);
