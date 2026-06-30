CREATE OR REPLACE FUNCTION documents.read_spaces(p_owner TEXT)
    RETURNS TABLE (
        id BIGINT,
        owner_id TEXT,
        icon TEXT,
        name TEXT,
        description TEXT
    )
    LANGUAGE plpgsql
AS
$$
    BEGIN
        -- Validate space exists
        IF NOT EXISTS (
            SELECT 1 FROM users."user" WHERE username = p_owner
        ) THEN
            RAISE EXCEPTION 'User with id % not found', p_owner;
        END IF;

        RETURN QUERY SELECT s.id, s.owner_id, s.icon, s.name, s.description FROM documents.space s WHERE s.owner_id = p_owner;
    END;
$$