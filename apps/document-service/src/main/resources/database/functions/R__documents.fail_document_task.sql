CREATE OR REPLACE FUNCTION documents.fail_document_task(
    p_document_id BIGINT,
    p_type TEXT
)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO documents.document_task (id, document_id, type, progress, max_progress, variant, created_at, updated_at)
    VALUES (p_document_id || ':' || p_type, p_document_id, p_type, 0, 1, 'ERROR', NOW(), NOW())
    ON CONFLICT (document_id, type) DO UPDATE
        SET variant    = 'ERROR',
            updated_at = NOW();
END;
$$;
