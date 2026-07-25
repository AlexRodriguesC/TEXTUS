CREATE OR REPLACE FUNCTION documents.read_chats_by_space_and_owner(
    p_space_id BIGINT,
    p_owner_id TEXT
)
    RETURNS TABLE (
                      id         UUID,
                      space_id   BIGINT,
                      owner_id   TEXT,
                      title      TEXT,
                      created_at TIMESTAMPTZ,
                      updated_at TIMESTAMPTZ
                  )
    LANGUAGE sql
AS
$$
SELECT c.id,
       c.space_id,
       c.owner_id,
       c.title,
       c.created_at,
       c.updated_at
FROM documents.chat c
WHERE c.space_id = p_space_id
  AND c.owner_id = p_owner_id
ORDER BY c.updated_at DESC;
$$;
