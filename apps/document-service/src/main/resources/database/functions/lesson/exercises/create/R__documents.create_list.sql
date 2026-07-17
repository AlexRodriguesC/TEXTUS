CREATE OR REPLACE FUNCTION documents.create_list(
    p_exercise_id  BIGINT,
    p_request      TEXT,
    p_answer       TEXT[]
) RETURNS BIGINT
    LANGUAGE plpgsql
AS $$
DECLARE
    v_list_exercise_id BIGINT;
    v_position INTEGER;
    v_item TEXT;
BEGIN
    -- Insert into list_exercise table
    INSERT INTO documents.list_exercise (exercise_id, request)
    VALUES (p_exercise_id, p_request)
    RETURNING id INTO v_list_exercise_id;

    -- Insert each answer item into list_answer table with position
    v_position := 1;
    FOREACH v_item IN ARRAY p_answer
        LOOP
            INSERT INTO documents.list_answer (list_exercise_id, position, item)
            VALUES (v_list_exercise_id, v_position, v_item);
            v_position := v_position + 1;
        END LOOP;

    RETURN v_list_exercise_id;
END;
$$;