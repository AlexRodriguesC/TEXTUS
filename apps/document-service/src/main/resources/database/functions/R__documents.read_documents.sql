CREATE OR REPLACE FUNCTION documents.read_documents(p_space_id BIGINT)
    RETURNS TABLE (
                    id BIGINT,
                      status document_status
                  )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT d.id, d.status
        FROM documents.document d
        WHERE d.space_id = p_space_id
        ORDER BY d.id;
END;
$$;