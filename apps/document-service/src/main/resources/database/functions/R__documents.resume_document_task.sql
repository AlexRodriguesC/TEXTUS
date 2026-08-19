CREATE OR REPLACE FUNCTION documents.resume_document_task(
    p_document_id BIGINT,
    p_type TEXT
)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE documents.document_task
    SET variant    = 'DEFAULT',
        updated_at = NOW()
    WHERE document_id = p_document_id
      AND type = p_type;
END;
$$;
