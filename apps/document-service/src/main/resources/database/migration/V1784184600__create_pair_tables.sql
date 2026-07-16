CREATE TABLE IF NOT EXISTS documents.pair_exercise
(
    id          BIGSERIAL PRIMARY KEY,
    exercise_id BIGINT REFERENCES documents.exercise (id) ON DELETE CASCADE,
    request     TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS documents.pair_answer
(
    id               BIGSERIAL PRIMARY KEY,
    pair_exercise_id BIGINT NOT NULL REFERENCES documents.pair_exercise (id) ON DELETE CASCADE,
    left_value       TEXT NOT NULL,
    right_value      TEXT NOT NULL,
    explanation      TEXT
);

CREATE INDEX idx_pair_answer_pair_exercise_id ON documents.pair_answer (pair_exercise_id);
