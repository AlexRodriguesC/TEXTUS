CREATE OR REPLACE FUNCTION documents.create_chapter(
    p_space_id BIGINT,
    p_path_id BIGINT,
    p_document_id BIGINT,
    p_index INTEGER,
    p_title TEXT,
    p_description TEXT
)
    RETURNS BIGINT
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_chapter_id BIGINT;
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM documents.learning_path
        WHERE id = p_path_id
          AND space_id = p_space_id
    ) THEN
        RAISE EXCEPTION 'Learning path with id % not found in space %', p_path_id, p_space_id;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM documents.document
        WHERE id = p_document_id
          AND space_id = p_space_id
    ) THEN
        RAISE EXCEPTION 'Document with id % not found in space %', p_document_id, p_space_id;
    END IF;

    INSERT INTO documents.chapter (path_id, document_id, index, title, description)
    VALUES (p_path_id, p_document_id, p_index, p_title, p_description)
    RETURNING id INTO v_chapter_id;

    RETURN v_chapter_id;
END;
$$;