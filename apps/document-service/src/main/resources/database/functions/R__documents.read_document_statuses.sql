CREATE OR REPLACE FUNCTION documents.read_document_statuses(p_space_id BIGINT)
    RETURNS TABLE (
                      status document_status
                  )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT d.status
        FROM documents.document d
        WHERE d.space_id = p_space_id
        ORDER BY d.id;
END;
$$;