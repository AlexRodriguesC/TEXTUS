CREATE OR REPLACE FUNCTION documents.read_lessons(p_chapter_id BIGINT)
    RETURNS TABLE
            (
                id            BIGINT,
                index         INTEGER,
                type          TEXT,
                current_topic BIGINT,
                attempts_left INTEGER,
                mastery       DOUBLE PRECISION,
                topics        BIGINT[]
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT n.id,
               n.index,
               n.type,
               n.current_topic,
               n.attempts_left,
               n.mastery,
               COALESCE(array_agg(tn.topic_id ORDER BY tn.topic_id) FILTER (WHERE tn.topic_id IS NOT NULL), ARRAY[]::BIGINT[]) AS topics
        FROM documents.lesson n
                 LEFT JOIN documents.topic_node tn ON tn.node_id = n.id
        WHERE n.chapter_id = p_chapter_id
        GROUP BY n.id, n.index, n.type, n.current_topic, n.attempts_left, n.mastery
        ORDER BY n.index;
END;
$$;