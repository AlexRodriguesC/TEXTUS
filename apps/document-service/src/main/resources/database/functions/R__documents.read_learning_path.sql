CREATE OR REPLACE FUNCTION documents.read_learning_path(p_space_id BIGINT)
    RETURNS TABLE
            (
                id          BIGINT,
                job_id      BIGINT,
                chapter_ids BIGINT[]
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT lp.id,
               lp.job_id,
               ARRAY_AGG(c.id ORDER BY c.index) AS chapter_ids
        FROM documents.learning_path lp
                 LEFT JOIN documents.chapter c ON c.path_id = lp.id
        WHERE lp.space_id = p_space_id
        GROUP BY lp.id, lp.job_id;
END;
$$;