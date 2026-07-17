CREATE OR REPLACE FUNCTION documents.read_list(p_list_exercise_id BIGINT)
    RETURNS TABLE(
                     request TEXT,
                     answer TEXT[]
                 )
    LANGUAGE plpgsql
AS $$
DECLARE
    v_answer_array TEXT[];
BEGIN
    -- Collect answer items into an array ordered by position
    SELECT ARRAY_AGG(la.item ORDER BY la.position)
    INTO v_answer_array
    FROM documents.list_answer la
    WHERE la.list_exercise_id = p_list_exercise_id;

    -- If no answers found, return empty array
    IF v_answer_array IS NULL THEN
        v_answer_array := '{}'::TEXT[];
    END IF;

    RETURN QUERY
        SELECT
            le.request,
            v_answer_array
        FROM documents.list_exercise le
        WHERE le.id = p_list_exercise_id;
END;
$$;