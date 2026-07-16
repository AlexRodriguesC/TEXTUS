CREATE OR REPLACE FUNCTION documents.create_pair(
    p_exercise_id  BIGINT,
    p_request      TEXT,
    p_left_value   TEXT,
    p_right_value  TEXT,
    p_explanation  TEXT
) RETURNS BIGINT
    LANGUAGE plpgsql
AS $$
DECLARE
    v_pair_exercise_id BIGINT;
BEGIN
    INSERT INTO documents.pair_exercise (exercise_id, request)
    VALUES (p_exercise_id, p_request)
    RETURNING id INTO v_pair_exercise_id;

    INSERT INTO documents.pair_answer (pair_exercise_id, left_value, right_value, explanation)
    VALUES (v_pair_exercise_id, p_left_value, p_right_value, p_explanation);

    RETURN v_pair_exercise_id;
END;
$$;
