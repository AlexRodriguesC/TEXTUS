CREATE OR REPLACE FUNCTION documents.read_chapters(p_space_id BIGINT)
    RETURNS TABLE
            (
                id          BIGINT,
                index       INTEGER,
                title       TEXT,
                description TEXT,
                topic_ids   BIGINT[]
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT c.id,
               c.index,
               c.title,
               c.description,
               ARRAY_AGG(t.id ORDER BY t.id) AS topic_ids
        FROM documents.chapter c
                 JOIN documents.topic t ON t.chapter_id = c.id
                 JOIN documents.page p ON p.id = t.page_id
        WHERE p.space_id = p_space_id
        GROUP BY c.id, c.index, c.title, c.description;
END;
$$;