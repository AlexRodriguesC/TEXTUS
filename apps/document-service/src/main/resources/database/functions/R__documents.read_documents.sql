CREATE OR REPLACE FUNCTION documents.read_documents(p_space_id BIGINT)
    RETURNS TABLE (
                    id BIGINT,
                    job_id BIGINT
                  )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT d.id, d.job_id
        FROM documents.document d
        WHERE d.space_id = p_space_id;
END;
$$;