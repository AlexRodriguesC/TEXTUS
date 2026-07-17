CREATE OR REPLACE FUNCTION documents.read_exercises_by_lesson(p_lesson_id BIGINT)
    RETURNS TABLE(
                     id BIGINT,
                     topic_id BIGINT,
                     family TEXT,
                     index INT,
                     explanation TEXT
                 )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT
            e.id,
            e.topic_id,
            e.family,
            e.index,
            e.explanation
        FROM documents.exercise e
        WHERE e.lesson_id = p_lesson_id
        ORDER BY e.index;
END;
$$;