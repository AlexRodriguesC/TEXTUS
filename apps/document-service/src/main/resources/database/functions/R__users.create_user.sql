CREATE OR REPLACE FUNCTION users.create_user(p_username TEXT, p_password TEXT)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
BEGIN
    INSERT INTO users.user(username, password) VALUES (p_username, p_password);
END;
$$;
