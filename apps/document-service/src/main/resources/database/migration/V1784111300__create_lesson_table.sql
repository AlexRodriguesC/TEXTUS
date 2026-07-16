CREATE TABLE IF NOT EXISTS documents.lesson
(
    id          BIGSERIAL PRIMARY KEY,
    chapter_id BIGINT REFERENCES chapter(id) ON DELETE CASCADE,
    index INTEGER,
    type TEXT,
    current_topic BIGINT,
    attempts_left INTEGER,
    mastery DOUBLE PRECISION
);