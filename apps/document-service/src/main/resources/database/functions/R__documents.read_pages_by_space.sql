CREATE OR REPLACE FUNCTION documents.read_pages_by_space(p_space_id BIGINT)
    RETURNS TABLE
            (
                id           BIGINT,
                document_id  BIGINT,
                page_number  INTEGER,
                title        TEXT,
                summary      TEXT
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT p.id, p.document_id, p.page_number, p.title, p.summary
        FROM documents.page p
        WHERE p.space_id = p_space_id
        ORDER BY p.id;
END;
$$;
