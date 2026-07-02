CREATE OR REPLACE FUNCTION documents.read_mind_map(p_space_id BIGINT)
    RETURNS TABLE (
                        job_id BIGINT,
                      topic_id BIGINT,
                      parent_topic_id BIGINT
                  )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT
            mm.job_id,
            child.topic_id,
            parent.topic_id AS parent_topic_id
        FROM documents.space s
                 JOIN documents.mind_map_node child
                      ON child.mind_map_id = s.mind_map_id
                 LEFT JOIN documents.mind_map_node parent
                           ON parent.id = child.parent_id
        JOIN documents.mind_map mm ON mm.id = s.mind_map_id
        WHERE s.id = p_space_id
        ORDER BY child.id;
END;
$$;