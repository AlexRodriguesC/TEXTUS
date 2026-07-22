CREATE OR REPLACE FUNCTION documents.create_mind_map_node(
    p_mind_map_id BIGINT,
    p_node_type TEXT,
    p_page_id BIGINT,
    p_topic_id BIGINT,
    p_parent_id BIGINT
)
    RETURNS BIGINT
    LANGUAGE plpgsql
AS
$$
    DECLARE
        v_node_id BIGINT;
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM documents.mind_map WHERE id = p_mind_map_id
        ) THEN
            RAISE EXCEPTION 'Mind map with id % not found', p_mind_map_id;
        END IF;

        IF p_node_type = 'PAGE' AND p_page_id IS NULL THEN
            RAISE EXCEPTION 'page_id is required when node_type is PAGE';
        END IF;

        IF p_node_type = 'TOPIC' AND p_topic_id IS NULL THEN
            RAISE EXCEPTION 'topic_id is required when node_type is TOPIC';
        END IF;

        INSERT INTO documents.mind_map_node(mind_map_id, node_type, page_id, topic_id, parent_id)
        VALUES (p_mind_map_id, p_node_type, p_page_id, p_topic_id, p_parent_id)
        RETURNING id INTO v_node_id;

        RETURN v_node_id;
    END;
$$
