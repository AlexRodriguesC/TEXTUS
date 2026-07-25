CREATE OR REPLACE FUNCTION documents.read_chat_memory_timestamps(
    p_conversation_id TEXT
)
    RETURNS TABLE (
                      message_timestamp TIMESTAMPTZ
                  )
    LANGUAGE sql
AS
$$
SELECT m."timestamp"
FROM spring_ai_chat_memory m
WHERE m.conversation_id = p_conversation_id
ORDER BY m."timestamp";
$$;
