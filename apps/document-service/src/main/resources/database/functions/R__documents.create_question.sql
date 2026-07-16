CREATE OR REPLACE FUNCTION documents.create_question(
    p_exercise_id  BIGINT,
    p_question     TEXT,
    p_answer       TEXT,
    p_explanation  TEXT,
    p_distractors  TEXT[]
) RETURNS BIGINT
    LANGUAGE plpgsql
AS $$
DECLARE
    v_id BIGINT;
BEGIN
    INSERT INTO documents.question (exercise_id, question, answer, explanation, distractors)
    VALUES (p_exercise_id, p_question, p_answer, p_explanation, p_distractors)
    RETURNING id INTO v_id;

    RETURN v_id;
END;
$$;
