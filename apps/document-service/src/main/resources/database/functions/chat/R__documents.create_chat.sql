CREATE OR REPLACE FUNCTION documents.create_chat(
    p_space_id  BIGINT,
    p_owner_id  TEXT,
    p_title     TEXT
)
    RETURNS UUID
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_chat_id UUID;
BEGIN
    INSERT INTO documents.chat (space_id, owner_id, title)
    VALUES (p_space_id, p_owner_id, p_title)
    RETURNING id INTO v_chat_id;

    RETURN v_chat_id;
END;
$$;
