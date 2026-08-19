CREATE OR REPLACE FUNCTION documents.read_document_tasks(p_document_id BIGINT)
    RETURNS TABLE
            (
                id           TEXT,
                document_id  BIGINT,
                type         TEXT,
                progress     INT,
                max_progress INT,
                variant      TEXT
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT t.id,
               t.document_id,
               t.type,
               t.progress,
               t.max_progress,
               t.variant
        FROM documents.document_task t
        WHERE t.document_id = p_document_id
        ORDER BY t.type;
END;
$$;
