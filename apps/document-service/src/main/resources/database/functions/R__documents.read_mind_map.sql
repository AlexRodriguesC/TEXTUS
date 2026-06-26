CREATE OR REPLACE FUNCTION documents.read_mind_map(p_space_id BIGINT)
    RETURNS TABLE (
                      chunk_id BIGINT,
                      parent_chunk_id BIGINT
                  )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT
            child.chunk_id,
            parent.chunk_id AS parent_chunk_id
        FROM documents.mind_map mm
                 JOIN documents.mind_map_node child
                      ON child.mind_map_id = mm.id
                 LEFT JOIN documents.mind_map_node parent
                           ON parent.id = child.parent_id
        WHERE mm.space_id = p_space_id
        ORDER BY child.id;
END;
$$;