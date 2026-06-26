CREATE OR REPLACE FUNCTION documents.create_chunk(
    p_space_id BIGINT,
    p_tokens_in INTEGER,
    p_tokens_out INTEGER,
    p_title TEXT,
    p_summary TEXT,
    p_key TEXT,
    p_keywords TEXT[],
    p_related_keys TEXT[],
    p_document_id BIGINT
)
    RETURNS BIGINT
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_chunk_id       BIGINT;
    v_keyword        TEXT;
    v_related_key    TEXT;
    v_found_key_id   BIGINT;
BEGIN
    -- Validate space exists
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

    INSERT INTO documents.chunk (space_id, document_id, tokens_in, tokens_out, title, summary, key)
    VALUES (p_space_id, p_document_id, p_tokens_in, p_tokens_out, p_title, p_summary, p_key)
    RETURNING id INTO v_chunk_id;

    FOREACH v_keyword IN ARRAY COALESCE(p_keywords, '{}') LOOP
            INSERT INTO documents.keyword (chunk_id, keyword)
            VALUES (v_chunk_id, v_keyword);
        END LOOP;

    FOREACH v_related_key IN ARRAY COALESCE(p_related_keys, '{}') LOOP
            v_found_key_id := NULL;

            SELECT k.id
            INTO v_found_key_id
            FROM documents.key k
            WHERE k.key = v_related_key
            LIMIT 1;

            IF v_found_key_id IS NULL THEN
                INSERT INTO documents.key (chunk_id, key)
                VALUES (v_chunk_id, v_related_key)
                RETURNING id INTO v_found_key_id;
            END IF;

            INSERT INTO documents.chunk_key (chunk_id, key_id)
            VALUES (v_chunk_id, v_found_key_id);
        END LOOP;

    RETURN v_chunk_id;
END;
$$;