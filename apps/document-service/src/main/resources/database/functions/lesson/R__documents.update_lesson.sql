
CREATE OR REPLACE FUNCTION documents.update_lesson(
    p_lesson_id      BIGINT,
    p_current_topic  BIGINT,
    p_attempts_left  INTEGER,
    p_mastery        DOUBLE PRECISION
) RETURNS VOID
    LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE documents.lesson
    SET current_topic = p_current_topic,
        attempts_left = p_attempts_left,
        mastery       = p_mastery
    WHERE id = p_lesson_id;
END;
$$;