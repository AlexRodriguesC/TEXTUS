CREATE OR REPLACE FUNCTION documents.read_question(p_exercise_id BIGINT)
    RETURNS TABLE(
                     id BIGINT,
                     question TEXT,
                     answer TEXT,
                     distractors TEXT[]
                 )
    LANGUAGE plpgsql
AS $$
DECLARE
    v_question_id BIGINT;
    v_distractors_array TEXT[];
BEGIN
    SELECT q.id
    INTO v_question_id
    FROM documents.question q
    WHERE q.exercise_id = p_exercise_id;

    SELECT ARRAY_AGG(d.distractor ORDER BY d.id)
    INTO v_distractors_array
    FROM documents.distractor d
    WHERE d.question_id = v_question_id;

    IF v_distractors_array IS NULL THEN
        v_distractors_array := '{}'::TEXT[];
    END IF;

    RETURN QUERY
        SELECT
            q.id,
            q.question,
            q.answer,
            v_distractors_array
        FROM documents.question q
        WHERE q.exercise_id = p_exercise_id;
END;
$$;
