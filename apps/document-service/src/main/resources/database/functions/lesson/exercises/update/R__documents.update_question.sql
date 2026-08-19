CREATE OR REPLACE FUNCTION documents.update_question(
    p_exercise_id  BIGINT,
    p_question     TEXT,
    p_answer       TEXT,
    p_distractors  TEXT[]
) RETURNS BIGINT
    LANGUAGE plpgsql
AS $$
DECLARE
    v_id BIGINT;
    v_distractor TEXT;
BEGIN
    UPDATE documents.question
    SET question = p_question,
        answer   = p_answer
    WHERE exercise_id = p_exercise_id
    RETURNING id INTO v_id;

    DELETE FROM documents.distractor
    WHERE question_id = v_id;

    IF p_distractors IS NOT NULL THEN
        FOREACH v_distractor IN ARRAY p_distractors
            LOOP
                INSERT INTO documents.distractor (question_id, distractor)
                VALUES (v_id, v_distractor);
            END LOOP;
    END IF;

    RETURN v_id;
END;
$$;
