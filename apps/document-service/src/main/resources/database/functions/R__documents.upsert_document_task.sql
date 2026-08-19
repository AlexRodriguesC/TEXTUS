CREATE OR REPLACE FUNCTION documents.upsert_document_task(
    p_document_id BIGINT,
    p_type TEXT,
    p_max_progress INT
)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO documents.document_task (id, document_id, type, progress, max_progress, variant, created_at, updated_at)
    VALUES (p_document_id || ':' || p_type, p_document_id, p_type, 0, p_max_progress, 'DEFAULT', NOW(), NOW())
    ON CONFLICT (document_id, type) DO UPDATE
        SET progress     = 0,
            max_progress = EXCLUDED.max_progress,
            variant      = 'DEFAULT',
            updated_at   = NOW();
END;
$$;
