CREATE TABLE IF NOT EXISTS documents.chunk
(
    id          BIGSERIAL PRIMARY KEY,
    space_id    BIGSERIAL REFERENCES documents.space (id) ON DELETE CASCADE,
    document_id BIGSERIAL REFERENCES documents.document (id),
    tokens_in   INTEGER,
    tokens_out  INTEGER,
    title       TEXT,
    summary     TEXT,
    key         TEXT
);

CREATE TABLE IF NOT EXISTS documents.page
(
    id          BIGSERIAL PRIMARY KEY,
    space_id    BIGSERIAL REFERENCES documents.space (id) ON DELETE CASCADE,
    document_id BIGSERIAL REFERENCES documents.document (id) ON DELETE CASCADE,
    tokens_in   INTEGER,
    tokens_out  INTEGER,
    title       TEXT,
    summary     TEXT
);

CREATE TABLE IF NOT EXISTS documents.topic
(
    id          BIGSERIAL PRIMARY KEY,
    page_id BIGSERIAL REFERENCES documents.page(id) ON DELETE CASCADE,
    tokens_in   INTEGER,
    tokens_out  INTEGER,
    title       TEXT,
    summary     TEXT
);

CREATE TABLE IF NOT EXISTS documents.label (
    id BIGSERIAL PRIMARY KEY,
    label TEXT
);

CREATE TABLE IF NOT EXISTS documents.topic_label (
    topic_id BIGSERIAL REFERENCES documents.topic(id) ON DELETE CASCADE,
    label_id BIGSERIAL REFERENCES documents.label(id) ON DELETE CASCADE,
    PRIMARY KEY (topic_id, label_id)
);
