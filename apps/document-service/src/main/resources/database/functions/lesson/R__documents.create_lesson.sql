CREATE OR REPLACE FUNCTION documents.create_lesson(
    p_chapter_id    BIGINT,
    p_index         INTEGER,
    p_type          TEXT,
    p_current_topic BIGINT,
    p_attempts_left INTEGER,
    p_mastery       DOUBLE PRECISION,
    p_topics        BIGINT[]
)
    RETURNS documents.lesson
    LANGUAGE plpgsql
AS $$
DECLARE
    new_lesson  documents.lesson;
    v_missing   BIGINT[];
BEGIN
    -- Validate chapter exists
    IF NOT EXISTS (SELECT 1 FROM documents.chapter WHERE id = p_chapter_id) THEN
        RAISE EXCEPTION 'Chapter with id % not found', p_chapter_id;
    END IF;

    -- Validate all given topic IDs exist
    SELECT ARRAY(
                   SELECT t FROM UNNEST(p_topics) t
                   WHERE NOT EXISTS (SELECT 1 FROM documents.topic WHERE id = t)
           ) INTO v_missing;

    IF array_length(v_missing, 1) > 0 THEN
        RAISE EXCEPTION 'Topic ids not found: %', v_missing;
    END IF;

    -- Insert lesson
    INSERT INTO documents.lesson (chapter_id, index, type, current_topic, attempts_left, mastery)
    VALUES (p_chapter_id, p_index, p_type, p_current_topic, p_attempts_left, p_mastery)
    RETURNING * INTO new_lesson;

    -- Link topics to this lesson
    INSERT INTO documents.topic_node (topic_id, node_id)
    SELECT DISTINCT t, new_lesson.id
    FROM UNNEST(p_topics) t
    ON CONFLICT (topic_id, node_id) DO NOTHING;

    RETURN new_lesson;
END;
$$;