CREATE OR REPLACE FUNCTION documents.read_lesson(p_lesson_id BIGINT)
    RETURNS TABLE(
                     id BIGINT,
                     chapter_id BIGINT,
                     index INT,
                     type TEXT,
                     current_topic BIGINT,
                     mastery DOUBLE PRECISION,
                     attempts_left INT,
                     topic_ids BIGINT[]
                 )
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT
            l.id,
            l.chapter_id,
            l.index,
            l.type,
            l.current_topic,
            l.mastery,
            l.attempts_left,
            COALESCE(
                    (SELECT array_agg(tn.topic_id ORDER BY tn.topic_id)
                     FROM documents.topic_node tn
                     WHERE tn.node_id = l.id),
                    '{}'::BIGINT[]
            ) AS topic_ids
        FROM documents.lesson l
        WHERE l.id = p_lesson_id;
END;
$$;