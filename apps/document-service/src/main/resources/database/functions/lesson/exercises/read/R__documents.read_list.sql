CREATE OR REPLACE FUNCTION documents.read_list(p_exercise_id BIGINT)
    RETURNS TABLE(
                     id BIGINT,
                     request TEXT,
                     answer TEXT[]
                 )
    LANGUAGE plpgsql
AS $$
DECLARE
    v_list_exercise_id BIGINT;
    v_answer_array TEXT[];
BEGIN
    SELECT le.id
    INTO v_list_exercise_id
    FROM documents.list_exercise le
    WHERE le.exercise_id = p_exercise_id;

    SELECT ARRAY_AGG(la.item ORDER BY la.position)
    INTO v_answer_array
    FROM documents.list_answer la
    WHERE la.list_exercise_id = v_list_exercise_id;

    IF v_answer_array IS NULL THEN
        v_answer_array := '{}'::TEXT[];
    END IF;

    RETURN QUERY
        SELECT
            le.id,
            le.request,
            v_answer_array
        FROM documents.list_exercise le
        WHERE le.exercise_id = p_exercise_id;
END;
$$;
