CREATE TYPE mind_map_status AS ENUM (
    'NOT_CREATED', 'GENERATING', 'DONE'
    );

CREATE TABLE IF NOT EXISTS documents.mind_map
(
    id     BIGSERIAL PRIMARY KEY,
    space_id BIGSERIAL REFERENCES documents.space(id),
    status mind_map_status DEFAULT 'NOT_CREATED'
)
