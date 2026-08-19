CREATE OR REPLACE FUNCTION documents.update_document_task_progress(
    p_document_id BIGINT,
    p_type TEXT,
    p_progress INT
)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE documents.document_task
    SET progress   = p_progress,
        updated_at = NOW()
    WHERE document_id = p_document_id
      AND type = p_type;
END;
$$;
