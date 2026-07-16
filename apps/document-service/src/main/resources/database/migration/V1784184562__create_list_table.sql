CREATE TABLE IF NOT EXISTS documents.list
(
    id           BIGSERIAL PRIMARY KEY,
    exercise_id BIGINT REFERENCES documents.exercise (id) ON DELETE CASCADE,
    request     TEXT NOT NULL,
    answer       TEXT[],
    explanation TEXT
)