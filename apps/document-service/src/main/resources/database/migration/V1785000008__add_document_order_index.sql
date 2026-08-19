DROP FUNCTION IF EXISTS documents.read_documents(BIGINT);

ALTER TABLE documents.document
    ADD COLUMN order_index INTEGER;

WITH ordered_documents AS (
    SELECT id,
           ROW_NUMBER() OVER (PARTITION BY space_id ORDER BY id) - 1 AS order_index
    FROM documents.document
)
UPDATE documents.document document
SET order_index = ordered_documents.order_index
FROM ordered_documents
WHERE document.id = ordered_documents.id;

ALTER TABLE documents.document
    ALTER COLUMN order_index SET NOT NULL;

CREATE INDEX document_space_id_order_index_idx
    ON documents.document (space_id, order_index);
