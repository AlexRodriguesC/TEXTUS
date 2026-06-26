CREATE TABLE IF NOT EXISTS documents.space
(
    id       BIGSERIAL PRIMARY KEY,
    owner_id TEXT REFERENCES users.user(username),
    mind_map_id BIGINT NULL REFERENCES documents.mind_map(id),
    name TEXT,
    description TEXT
)
