CREATE OR REPLACE FUNCTION documents.read_next_exercise(p_node_id BIGINT)
    RETURNS TABLE (exercise_id BIGINT, family TEXT)
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT e.id, e.family
        FROM documents.exercise e
        WHERE e.node_id = p_node_id
          AND NOT EXISTS (
            SELECT 1 FROM documents.attempt a
            WHERE a.exercise_id = e.id AND a.node_id = p_node_id
        )
        ORDER BY e.index
        LIMIT 1;
END;
$$;