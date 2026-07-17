CREATE TABLE IF NOT EXISTS documents.list_exercise
(
    id          BIGSERIAL PRIMARY KEY,
    exercise_id BIGINT REFERENCES documents.exercise (id) ON DELETE CASCADE,
    request     TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS documents.list_answer
(
    id              BIGSERIAL PRIMARY KEY,
    list_exercise_id BIGINT NOT NULL REFERENCES documents.list_exercise (id) ON DELETE CASCADE,
    position        INTEGER NOT NULL,  -- to maintain order
    item            TEXT NOT NULL,
    UNIQUE (list_exercise_id, position)
);

CREATE INDEX idx_list_answer_list_exercise_id ON documents.list_answer (list_exercise_id);
CREATE INDEX idx_list_answer_position ON documents.list_answer (position);