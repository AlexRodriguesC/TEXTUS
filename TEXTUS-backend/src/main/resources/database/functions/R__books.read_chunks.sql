CREATE OR REPLACE FUNCTION books.read_chunks(p_id BIGINT)
    RETURNS TABLE
            (
                id           BIGINT,
                book_id      BIGINT,
                tokens_in    INTEGER,
                tokens_out   INTEGER,
                page         INTEGER,
                title        TEXT,
                summary      TEXT,
                key          TEXT,
                questions    TEXT[],
                keywords     TEXT[],
                related_keys TEXT[]
            )
    LANGUAGE plpgsql
AS
$$
BEGIN
    RETURN QUERY
        SELECT c.id,
               c.book_id,
               c.tokens_in,
               c.tokens_out,
               c.page,
               c.title,
               c.summary,
               c.key,
               COALESCE((SELECT array_agg(q.question ORDER BY q.id)
                         FROM books.question q
                         WHERE q.chunk_id = c.id), '{}'::TEXT[]),
               COALESCE((SELECT array_agg(k.keyword ORDER BY k.id)
                         FROM books.keyword k
                         WHERE k.chunk_id = c.id), '{}'::TEXT[]),
               COALESCE((SELECT array_agg(rk.key ORDER BY ck.id)
                         FROM books.chunk_key ck
                                  JOIN books.key rk ON rk.id = ck.key_id
                         WHERE ck.chunk_id = c.id), '{}'::TEXT[])
        FROM books.chunk c
        WHERE c.book_id = p_id
        ORDER BY c.page, c.id;
END;
$$;