CREATE OR REPLACE FUNCTION documents.read_pages(p_document_id BIGINT)
    RETURNS TABLE
            (
                id           BIGINT,
                space_id BIGINT,
                document_id     BIGINT,
                title        TEXT,
                summary      TEXT
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT p.id, p.space_id, p.document_id, p.title, p.summary FROM documents.page p
        WHERE p.document_id = p_document_id;
END;
$$;