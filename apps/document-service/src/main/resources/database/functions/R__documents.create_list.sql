CREATE OR REPLACE FUNCTION documents.create_list(
    p_exercise_id  BIGINT,
    p_request      TEXT,
    p_answer       TEXT[],
    p_explanation  TEXT
) RETURNS BIGINT
    LANGUAGE plpgsql
AS $$
DECLARE
    v_id BIGINT;
BEGIN
    INSERT INTO documents.list (exercise_id, request, answer, explanation)
    VALUES (p_exercise_id, p_request, p_answer, p_explanation)
    RETURNING id INTO v_id;

    RETURN v_id;
END;
$$;
