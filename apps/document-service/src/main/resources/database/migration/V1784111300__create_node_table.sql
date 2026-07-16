CREATE TABLE IF NOT EXISTS documents.lesson2
(
    id          BIGSERIAL PRIMARY KEY,
    index INTEGER,
    type TEXT,
    chapter_id BIGINT REFERENCES chapter(id) ON DELETE CASCADE
);