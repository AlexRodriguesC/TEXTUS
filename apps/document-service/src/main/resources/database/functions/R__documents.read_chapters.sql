CREATE OR REPLACE FUNCTION documents.read_chapters(p_space_id BIGINT)
    RETURNS TABLE
            (
                id          BIGINT,
                document_id BIGINT,
                index       INTEGER,
                title       TEXT,
                description TEXT
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT c.id,
               c.document_id,
               c.index,
               c.title,
               c.description
        FROM documents.chapter c
                 JOIN documents.learning_path p ON p.id = c.path_id
    WHERE p.space_id = p_space_id
    ORDER BY c.index;
END;
$$;