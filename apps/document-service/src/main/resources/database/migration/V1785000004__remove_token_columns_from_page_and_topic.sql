ALTER TABLE documents.page DROP COLUMN IF EXISTS tokens_in;
ALTER TABLE documents.page DROP COLUMN IF EXISTS tokens_out;

ALTER TABLE documents.topic DROP COLUMN IF EXISTS tokens_in;
ALTER TABLE documents.topic DROP COLUMN IF EXISTS tokens_out;
