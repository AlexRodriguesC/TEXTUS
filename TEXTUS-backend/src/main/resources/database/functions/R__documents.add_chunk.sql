CREATE OR REPLACE FUNCTION documents.add_chunk(
    p_tokens_in INTEGER,
    p_tokens_out INTEGER,
    p_title TEXT,
    p_summary TEXT,
    p_key TEXT,
    p_questions TEXT[],
    p_keywords TEXT[],
    p_related_keys TEXT[]
)
    RETURNS BIGINT
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_chunk_i        BIGINT;
    v_question       TEXT;
    v_keyword        TEXT;
    v_related_key    TEXT;
    v_found_key_id   BIGINT;
BEGIN
    INSERT INTO documents.chunk (tokens_in, tokens_out, title, summary, key)
    VALUES (p_tokens_in, p_tokens_out, p_title, p_summary, p_key)
    RETURNING id INTO v_chunk_i;

    FOREACH v_question IN ARRAY COALESCE(p_questions, '{}') LOOP
            INSERT INTO documents.question (chunk_id, question)
            VALUES (v_chunk_i, v_question);
        END LOOP;


    FOREACH v_keyword IN ARRAY COALESCE(p_keywords, '{}') LOOP
            INSERT INTO documents.keyword (chunk_id, keyword)
            VALUES (v_chunk_i, v_keyword);
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
                VALUES (v_chunk_i, v_related_key)
                RETURNING id INTO v_found_key_id;
            END IF;

            INSERT INTO documents.chunk_key (chunk_id, key_id)
            VALUES (v_chunk_i, v_found_key_id);
        END LOOP;

    RETURN v_chunk_i;
END;
$$;