CREATE OR REPLACE FUNCTION documents.create_chapter(
    p_space_id BIGINT,
    p_index INTEGER,
    p_title TEXT,
    p_description TEXT,
    p_topics BIGINT[]
)
    RETURNS BIGINT
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_chapter_id BIGINT;
BEGIN
    -- Validate space exists
    IF NOT EXISTS (
        SELECT 1 FROM documents.space WHERE id = p_space_id
    ) THEN
        RAISE EXCEPTION 'Space with id % not found', p_space_id;
    END IF;

    INSERT INTO documents.chapter (index, title, description)
    VALUES (p_index, p_title, p_description)
    RETURNING id INTO v_chapter_id;

    UPDATE documents.topic
    SET chapter_id = v_chapter_id
    WHERE id = ANY (p_topics);

    RETURN v_chapter_id;
END;
$$;