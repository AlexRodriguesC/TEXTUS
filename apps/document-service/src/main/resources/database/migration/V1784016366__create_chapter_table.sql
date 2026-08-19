CREATE TABLE IF NOT EXISTS documents.chapter
(
    id          BIGSERIAL PRIMARY KEY,
    path_id     BIGSERIAL REFERENCES documents.learning_path(id) ON DELETE CASCADE ,
    index       INTEGER,
    title       TEXT,
    description TEXT
);
