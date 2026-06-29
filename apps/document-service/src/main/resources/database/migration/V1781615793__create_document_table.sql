CREATE TYPE document_status AS ENUM (
    'RECEIVED', 'PROCESSING', 'DONE'
    );

CREATE TABLE IF NOT EXISTS documents.document
(
    id       BIGSERIAL PRIMARY KEY,
    space_id BIGSERIAL REFERENCES documents.space(id) ON DELETE CASCADE,
    status document_status DEFAULT 'RECEIVED'
)
