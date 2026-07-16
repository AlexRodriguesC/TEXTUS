CREATE TABLE IF NOT EXISTS documents.question
(
    id           BIGSERIAL PRIMARY KEY,
    exercise_id BIGINT REFERENCES documents.exercise (id) ON DELETE CASCADE,
    question     TEXT NOT NULL,
    answer       TEXT,
    explanation TEXT,
    distractors  TEXT[]
)