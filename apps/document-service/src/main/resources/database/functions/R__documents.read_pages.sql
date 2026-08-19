CREATE OR REPLACE FUNCTION documents.read_pages(p_document_id BIGINT)
    RETURNS TABLE
            (
                id           BIGINT,
                space_id BIGINT,
                document_id     BIGINT,
                page_number  INTEGER,
                title        TEXT,
                summary      TEXT
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT p.id, p.space_id, p.document_id, p.page_number, p.title, p.summary FROM documents.page p
        WHERE p.document_id = p_document_id;
END;
$$;
