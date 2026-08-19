CREATE TABLE IF NOT EXISTS documents.topic_label
(
    topic_id BIGSERIAL REFERENCES documents.topic (id) ON DELETE CASCADE,
    label_id BIGSERIAL REFERENCES documents.label (id) ON DELETE CASCADE,
    PRIMARY KEY (topic_id, label_id)
);