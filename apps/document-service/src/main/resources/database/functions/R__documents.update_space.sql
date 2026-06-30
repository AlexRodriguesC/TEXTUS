CREATE OR REPLACE FUNCTION documents.update_space(p_id BIGINT, p_icon TEXT, p_name TEXT, p_description TEXT)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
    BEGIN
        UPDATE documents.space SET icon = p_icon, name = p_name, description = p_description WHERE id = p_id;
    END;
$$
