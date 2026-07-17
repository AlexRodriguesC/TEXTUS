CREATE OR REPLACE FUNCTION documents.read_group(p_exercise_id BIGINT)
    RETURNS TABLE(
                     group_exercise_id BIGINT,
                     request TEXT,
                     answer JSONB
                 )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT
            ge.id AS group_exercise_id,
            ge.request,
            -- Construct the answer JSONB from group_answer and group_answer_item tables
            (
                SELECT jsonb_object_agg(ga.label, items.items_array)
                FROM documents.group_answer ga
                         LEFT JOIN LATERAL (
                    SELECT jsonb_agg(gai.item ORDER BY gai.id) AS items_array
                    FROM documents.group_answer_item gai
                    WHERE gai.group_answer_id = ga.id
                    ) items ON true
                WHERE ga.group_exercise_id = ge.id
            ) AS answer
        FROM documents.group_exercise ge
        WHERE ge.id = p_exercise_id;
END;
$$;