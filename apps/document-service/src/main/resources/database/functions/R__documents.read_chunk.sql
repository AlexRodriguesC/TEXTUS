CREATE OR REPLACE FUNCTION documents.read_chunk(p_chunk_id BIGINT)
    RETURNS TABLE
            (
                id           BIGINT,
                tokens_in    INTEGER,
                tokens_out   INTEGER,
                title        TEXT,
                summary      TEXT,
                key          TEXT,
                keywords     TEXT[],
                related_keys TEXT[]
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT c.id,
               c.tokens_in,
               c.tokens_out,
               c.title,
               c.summary,
               c.key,
               COALESCE((SELECT array_agg(k.keyword ORDER BY k.id)
                         FROM documents.keyword k
                         WHERE k.chunk_id = c.id), '{}'::TEXT[]),
               COALESCE((SELECT array_agg(rk.key ORDER BY ck.id)
                         FROM documents.chunk_key ck
                                  JOIN documents.key rk ON rk.id = ck.key_id
                         WHERE ck.chunk_id = c.id), '{}'::TEXT[])
        FROM documents.chunk c
        WHERE c.id = p_chunk_id
        ORDER BY c.id;
END;
$$;