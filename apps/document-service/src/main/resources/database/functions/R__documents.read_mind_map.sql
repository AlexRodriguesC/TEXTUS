CREATE OR REPLACE FUNCTION documents.read_mind_map(p_space_id BIGINT)
    RETURNS TABLE (
                      job_id            BIGINT,
                      node_type         TEXT,
                      page_id           BIGINT,
                      topic_id          BIGINT,
                      parent_page_id    BIGINT,
                      parent_topic_id   BIGINT,
                      parent_node_type  TEXT
                  )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT
            mm.job_id,
            child.node_type,
            child.page_id,
            child.topic_id,
            parent.page_id AS parent_page_id,
            parent.topic_id AS parent_topic_id,
            parent.node_type AS parent_node_type
        FROM documents.space s
                 JOIN documents.mind_map mm ON mm.id = s.mind_map_id
                 LEFT JOIN documents.mind_map_node child
                           ON child.mind_map_id = mm.id
                 LEFT JOIN documents.mind_map_node parent
                           ON parent.id = child.parent_id
        WHERE s.id = p_space_id
        ORDER BY child.id;
END;
$$;
