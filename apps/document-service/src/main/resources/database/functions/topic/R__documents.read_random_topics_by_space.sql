-- Returns up to p_limit random topics for a space.
-- ORDER BY random() is acceptable for typical per-space topic counts; if topic volume
-- grows substantially, consider TABLESAMPLE or reservoir sampling instead.
CREATE OR REPLACE FUNCTION documents.read_random_topics_by_space(
    p_space_id BIGINT,
    p_limit    INT
)
    RETURNS TABLE (
                      id             BIGINT,
                      page_id        BIGINT,
                      page_number    INTEGER,
                      src_file_name  TEXT,
                      title          TEXT,
                      summary        TEXT,
                      labels         TEXT[],
                      exercise_types TEXT[]
                  )
    LANGUAGE sql
AS $$
SELECT
    t.id,
    t.page_id,
    p.page_number,
    d.name AS src_file_name,
    t.title,
    t.summary,
    COALESCE(ARRAY_AGG(DISTINCT l.label ORDER BY l.label) FILTER (WHERE l.label IS NOT NULL), '{}'),
    COALESCE(ARRAY_AGG(DISTINCT tt.type ORDER BY tt.type) FILTER (WHERE tt.type IS NOT NULL), '{}')
FROM documents.topic t
         LEFT JOIN documents.topic_label tl ON tl.topic_id = t.id
         LEFT JOIN documents.label l ON l.id = tl.label_id
         LEFT JOIN documents.topic_type tt ON tt.topic_id = t.id
         JOIN documents.page p ON p.id = t.page_id
         JOIN documents.document d ON d.id = p.document_id
WHERE p.space_id = p_space_id
GROUP BY t.id, t.page_id, p.page_number, d.name, t.title, t.summary
ORDER BY random()
LIMIT p_limit
$$;
