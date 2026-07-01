CREATE OR REPLACE FUNCTION documents.read_topics_by_page(
    p_page_id BIGINT
)
    RETURNS TABLE (
                      id          BIGINT,
                      page_id     BIGINT,
                      tokens_in   INTEGER,
                      tokens_out  INTEGER,
                      title       TEXT,
                      summary     TEXT,
                      labels      TEXT[]
                  )
    LANGUAGE sql
AS $$
SELECT
    t.id,
    t.page_id,
    t.tokens_in,
    t.tokens_out,
    t.title,
    t.summary,
    COALESCE(ARRAY_AGG(l.label ORDER BY l.label) FILTER (WHERE l.label IS NOT NULL), '{}')
FROM documents.topic t
         LEFT JOIN documents.topic_label tl ON tl.topic_id = t.id
         LEFT JOIN documents.label l ON l.id = tl.label_id
WHERE t.page_id = p_page_id
GROUP BY t.id, t.page_id, t.tokens_in, t.tokens_out, t.title, t.summary
$$;