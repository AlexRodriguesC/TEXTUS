
CREATE OR REPLACE FUNCTION documents.update_lesson(
    p_lesson_id      BIGINT,
    p_current_topic  BIGINT,
    p_attempts_left  INTEGER,
    p_mastery        DOUBLE PRECISION,
    p_topic_ids      BIGINT[] DEFAULT '{}'::BIGINT[]
) RETURNS VOID
    LANGUAGE plpgsql
AS $$
DECLARE
    v_topic_id BIGINT;
BEGIN
    UPDATE documents.lesson
    SET current_topic = p_current_topic,
        attempts_left = p_attempts_left,
        mastery       = p_mastery
    WHERE id = p_lesson_id;

    IF p_topic_ids IS NOT NULL THEN
        FOREACH v_topic_id IN ARRAY p_topic_ids
            LOOP
                PERFORM documents.add_topic_to_lesson(p_lesson_id, v_topic_id);
            END LOOP;
    END IF;
END;
$$;
