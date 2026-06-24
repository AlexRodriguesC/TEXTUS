CREATE OR REPLACE FUNCTION users.read_user_by_username(p_username TEXT)
    RETURNS TABLE(username TEXT, password TEXT)
    LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT * FROM users.user u WHERE u.username = p_username;
END;
$$;