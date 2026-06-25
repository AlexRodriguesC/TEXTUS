CREATE TABLE IF NOT EXISTS documents.space
(
    id       BIGSERIAL PRIMARY KEY,
    owner_id TEXT REFERENCES users.user(username),
    name TEXT,
    description TEXT
)
