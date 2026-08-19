CREATE OR REPLACE FUNCTION documents.create_space(p_owner TEXT, p_icon TEXT, p_name TEXT, p_description TEXT)
    RETURNS BIGINT
    LANGUAGE plpgsql
AS
$$
    DECLARE
        v_id BIGINT;
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM users."user" u WHERE u.username = p_owner
        ) THEN
            RAISE EXCEPTION 'User with username %s not found', p_owner;
        END IF;

        INSERT INTO documents.space(owner_id, icon, name, description) VALUES (p_owner, p_icon, p_name, p_description) RETURNING id INTO v_id;
        RETURN v_id;
    END;
$$