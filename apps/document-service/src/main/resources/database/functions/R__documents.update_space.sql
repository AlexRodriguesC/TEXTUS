CREATE OR REPLACE FUNCTION documents.update_space(p_id BIGINT, p_name TEXT, p_description TEXT)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
    BEGIN
        UPDATE documents.space s SET s.name = p_name, s.description = p_description WHERE s.id = p_id;
    END;
$$
