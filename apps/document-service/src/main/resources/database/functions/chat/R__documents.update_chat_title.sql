CREATE OR REPLACE FUNCTION documents.update_chat_title(
    p_chat_id UUID,
    p_title   TEXT
)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
BEGIN
    UPDATE documents.chat
    SET title      = p_title,
        updated_at = NOW()
    WHERE id = p_chat_id;
END;
$$;
