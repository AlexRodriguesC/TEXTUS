CREATE TABLE IF NOT EXISTS documents.learning_path_settings
(
    id                  BIGSERIAL PRIMARY KEY,
    path_id             BIGSERIAL REFERENCES documents.learning_path (id) ON DELETE CASCADE,
    question_percentage INTEGER DEFAULT 25,
    list_percentage     INTEGER DEFAULT 25,
    group_percentage    INTEGER DEFAULT 25,
    pair_percentage     INTEGER DEFAULT 25
);
