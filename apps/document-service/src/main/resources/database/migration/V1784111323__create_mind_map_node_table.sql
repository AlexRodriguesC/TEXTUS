CREATE TABLE IF NOT EXISTS documents.mind_map_node
(
    id          BIGSERIAL PRIMARY KEY,
    mind_map_id BIGINT NOT NULL REFERENCES documents.mind_map (id) ON DELETE CASCADE,
    node_type   TEXT NOT NULL CHECK (node_type IN ('PAGE', 'TOPIC')),
    page_id     BIGINT NULL REFERENCES documents.page (id) ON DELETE CASCADE,
    topic_id    BIGINT NULL REFERENCES documents.topic (id) ON DELETE CASCADE,
    parent_id   BIGINT NULL REFERENCES documents.mind_map_node (id) ON DELETE CASCADE,
    CHECK (
        (node_type = 'PAGE' AND page_id IS NOT NULL AND topic_id IS NULL) OR
        (node_type = 'TOPIC' AND topic_id IS NOT NULL AND page_id IS NULL)
    ),
    UNIQUE (mind_map_id, page_id),
    UNIQUE (mind_map_id, topic_id)
);
