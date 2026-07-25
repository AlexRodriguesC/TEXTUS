CREATE OR REPLACE FUNCTION documents.touch_chat_updated_at(
    p_chat_id UUID
)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE documents.chat
    SET updated_at = NOW()
    WHERE id = p_chat_id;
END;
$$;
