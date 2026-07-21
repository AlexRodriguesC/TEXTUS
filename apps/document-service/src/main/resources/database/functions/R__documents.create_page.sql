CREATE OR REPLACE FUNCTION documents.create_page(
    p_space_id BIGINT,
    p_document_id BIGINT,
    p_title TEXT,
    p_summary TEXT
)
    RETURNS BIGINT
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_page_id       BIGINT;
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM documents.space WHERE id = p_space_id
    ) THEN
        RAISE EXCEPTION 'Space with id % not found', p_space_id;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM documents.document WHERE id = p_document_id
    ) THEN
        RAISE EXCEPTION 'Document with id % not found', p_space_id;
    END IF;

    INSERT INTO documents.page (space_id, document_id, title, summary)
    VALUES (p_space_id, p_document_id, p_title, p_summary)
    RETURNING id INTO v_page_id;


    RETURN v_page_id;
END;
$$;
