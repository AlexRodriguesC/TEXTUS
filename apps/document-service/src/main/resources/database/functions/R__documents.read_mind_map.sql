CREATE OR REPLACE FUNCTION documents.read_mind_map(p_space_id BIGINT)
    RETURNS TABLE (
                      topic_id BIGINT,
                      parent_topic_id BIGINT
                  )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT
            child.topic_id,
            parent.topic_id AS parent_topic_id
        FROM documents.space s
                 JOIN documents.mind_map_node child
                      ON child.mind_map_id = s.mind_map_id
                 LEFT JOIN documents.mind_map_node parent
                           ON parent.id = child.parent_id
        WHERE s.id = p_space_id
        ORDER BY child.id;
END;
$$;