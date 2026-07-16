CREATE OR REPLACE FUNCTION documents.create_attempt(
    p_exercise_id BIGINT,
    p_lesson_id   BIGINT,
    p_user_id     TEXT,
    p_response    JSONB,
    p_is_correct  BOOLEAN,
    p_feedback    TEXT
)
    RETURNS documents.attempt
    LANGUAGE plpgsql
AS $$
DECLARE
    new_attempt documents.attempt;
BEGIN
    INSERT INTO documents.attempt (exercise_id, lesson_id, user_id, response, is_correct, feedback)
    VALUES (p_exercise_id, p_lesson_id, p_user_id, p_response, p_is_correct, p_feedback)
    RETURNING * INTO new_attempt;

    RETURN new_attempt;
END;
$$;