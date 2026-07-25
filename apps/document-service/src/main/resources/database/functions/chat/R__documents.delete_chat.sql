CREATE OR REPLACE FUNCTION documents.delete_chat(
    p_chat_id UUID
)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
BEGIN
    DELETE FROM documents.chat WHERE id = p_chat_id;
END;
$$;
