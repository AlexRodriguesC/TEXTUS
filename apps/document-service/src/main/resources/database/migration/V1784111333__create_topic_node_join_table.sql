CREATE TABLE IF NOT EXISTS documents.topic_node
(
    topic_id BIGSERIAL REFERENCES documents.topic (id) ON DELETE CASCADE,
    node_id BIGSERIAL REFERENCES documents.lesson (id) ON DELETE CASCADE,
    PRIMARY KEY (topic_id, node_id)
);