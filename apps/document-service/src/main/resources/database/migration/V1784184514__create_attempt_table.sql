CREATE TABLE IF NOT EXISTS documents.attempt
(
    id          BIGSERIAL PRIMARY KEY,
    exercise_id BIGINT REFERENCES documents.exercise (id) ON DELETE CASCADE,
    lesson_id     BIGINT REFERENCES documents.lesson (id) ON DELETE CASCADE,
    user_id     TEXT REFERENCES users.user (username) ON DELETE CASCADE,
    response    JSONB   NOT NULL
);