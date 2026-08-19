CREATE OR REPLACE FUNCTION documents.read_learning_path(p_space_id BIGINT)
    RETURNS TABLE
            (
                id          BIGINT,
                job_id      BIGINT,
                chapter_ids BIGINT[],
                question_percentage INTEGER,
                list_percentage     INTEGER,
                group_percentage    INTEGER,
                pair_percentage     INTEGER
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT lp.id,
               lp.job_id,
               ARRAY_AGG(c.id ORDER BY c.index) AS chapter_ids,
               s.question_percentage,
               s.list_percentage,
               s.group_percentage,
               s.pair_percentage
        FROM documents.learning_path lp
                 LEFT JOIN documents.chapter c ON c.path_id = lp.id
                JOIN documents.learning_path_settings s ON s.path_id = lp.id
        WHERE lp.space_id = p_space_id
        GROUP BY lp.id, lp.job_id, s.question_percentage, s.list_percentage, s.group_percentage, s.pair_percentage;
END;
$$;