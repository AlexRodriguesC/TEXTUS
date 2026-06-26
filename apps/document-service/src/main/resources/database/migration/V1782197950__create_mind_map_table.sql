CREATE TYPE mind_map_status AS ENUM (
    'NOT_CREATED', 'GENERATING', 'DONE'
    );

CREATE TABLE IF NOT EXISTS documents.mind_map
(
    id     BIGSERIAL PRIMARY KEY,
    status mind_map_status DEFAULT 'NOT_CREATED'
)
