CREATE OR REPLACE FUNCTION documents.update_list(
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
    UPDATE documents.list_exercise
    SET request = p_request
    WHERE exercise_id = p_exercise_id
    RETURNING id INTO v_list_exercise_id;

    DELETE FROM documents.list_answer
    WHERE list_exercise_id = v_list_exercise_id;

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