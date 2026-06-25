CREATE OR REPLACE FUNCTION documents.read_spaces(p_owner TEXT)
    RETURNS TABLE (
       id BIGINT,
        name TEXT,
        description TEXT
    )
    LANGUAGE plpgsql
AS
$$
    BEGIN
        RETURN QUERY SELECT s.id, s.name, s.description FROM documents.space s WHERE s.owner_id = p_owner;
    END;
$$