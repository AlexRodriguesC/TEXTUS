CREATE TABLE IF NOT EXISTS documents.group_exercise
(
    id      BIGSERIAL PRIMARY KEY,
    exercise_id BIGINT REFERENCES documents.exercise (id) ON DELETE CASCADE,
    request TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS documents.group_answer
(
    id              BIGSERIAL PRIMARY KEY,
    group_exercise_id BIGINT NOT NULL REFERENCES documents.group_exercise (id) ON DELETE CASCADE,
    explanation     TEXT
);

CREATE TABLE IF NOT EXISTS documents.group_answer_label
(
    id              BIGSERIAL PRIMARY KEY,
    group_answer_id BIGINT NOT NULL REFERENCES documents.group_answer (id) ON DELETE CASCADE,
    label           TEXT NOT NULL,
    UNIQUE (group_answer_id, label)
);

CREATE TABLE IF NOT EXISTS documents.group_answer_item
(
    id       BIGSERIAL PRIMARY KEY,
    label_id BIGINT NOT NULL REFERENCES documents.group_answer_label (id) ON DELETE CASCADE,
    item     TEXT NOT NULL
);

CREATE INDEX idx_group_answer_group_exercise_id ON documents.group_answer (group_exercise_id);
CREATE INDEX idx_group_answer_label_group_answer_id ON documents.group_answer_label (group_answer_id);
CREATE INDEX idx_group_answer_item_label_id ON documents.group_answer_item (label_id);
