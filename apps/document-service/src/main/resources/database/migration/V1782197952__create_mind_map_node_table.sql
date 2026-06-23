CREATE TABLE IF NOT EXISTS documents.mind_map_node
(
    id          BIGSERIAL PRIMARY KEY,
    mind_map_id BIGINT NOT NULL REFERENCES documents.mind_map (id) ON DELETE CASCADE,
    chunk_id    BIGINT NOT NULL REFERENCES documents.chunk (id),
    parent_id   BIGINT NULL REFERENCES documents.mind_map_node (id) ON DELETE CASCADE,
    UNIQUE (mind_map_id, chunk_id) -- A chunk can only belong to one mind map node
);