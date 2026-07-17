CREATE OR REPLACE FUNCTION documents.read_question(p_question_id BIGINT)
    RETURNS TABLE(
                     question TEXT,
                     answer TEXT,
                     distractors TEXT[]
                 )
    LANGUAGE plpgsql
AS $$
DECLARE
    v_distractors_array TEXT[];
BEGIN
    -- Get distractors from the distractor table
    SELECT ARRAY_AGG(d.distractor ORDER BY d.id)
    INTO v_distractors_array
    FROM documents.distractor d
    WHERE d.question_id = p_question_id;

    -- If no distractors found, return empty array
    IF v_distractors_array IS NULL THEN
        v_distractors_array := '{}'::TEXT[];
    END IF;

    RETURN QUERY
        SELECT
            q.question,
            q.answer,
            v_distractors_array
        FROM documents.question q
        WHERE q.id = p_question_id;
END;
$$;