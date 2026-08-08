CREATE OR REPLACE FUNCTION documents.read_documents(p_space_id BIGINT)
    RETURNS TABLE (
                    id BIGINT,
                    job_id BIGINT,
                    name TEXT,
                    order_index INTEGER
                  )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT d.id, d.job_id, d.name, d.order_index
        FROM documents.document d
        WHERE d.space_id = p_space_id
        ORDER BY d.order_index, d.id;
END;
$$;