CREATE OR REPLACE FUNCTION documents.add_topic_to_lesson(
    p_lesson_id BIGINT,
    p_topic_id  BIGINT
) RETURNS VOID
    LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM documents.lesson WHERE id = p_lesson_id) THEN
        RAISE EXCEPTION 'Lesson with id % not found', p_lesson_id;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM documents.topic WHERE id = p_topic_id) THEN
        RAISE EXCEPTION 'Topic with id % not found', p_topic_id;
    END IF;

    INSERT INTO documents.topic_node (topic_id, node_id)
    VALUES (p_topic_id, p_lesson_id)
    ON CONFLICT (topic_id, node_id) DO NOTHING;
END;
$$;
