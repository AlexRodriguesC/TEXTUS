CREATE OR REPLACE FUNCTION documents.create_node(
    p_chapter_id BIGINT,
    p_index INTEGER,
    p_type TEXT,
    p_topics BIGINT[]
)
    RETURNS BIGINT
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_node_id BIGINT;
    v_missing BIGINT[];
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM documents.chapter WHERE id = p_chapter_id
    ) THEN
        RAISE EXCEPTION 'Chapter with id % not found', p_chapter_id;
    END IF;

    SELECT ARRAY(
                   SELECT t FROM UNNEST(p_topics) t
                   WHERE NOT EXISTS (SELECT 1 FROM documents.topic WHERE id = t)
           ) INTO v_missing;

    IF array_length(v_missing, 1) > 0 THEN
        RAISE EXCEPTION 'Topic ids not found: %', v_missing;
    END IF;

    INSERT INTO documents.node (index, type, chapter_id)
    VALUES (p_index, p_type, p_chapter_id)
    RETURNING id INTO v_node_id;

    INSERT INTO documents.topic_node (topic_id, node_id)
    SELECT DISTINCT t, v_node_id
    FROM UNNEST(p_topics) t
    ON CONFLICT (topic_id, node_id) DO NOTHING;

    RETURN v_node_id;
END;
$$;