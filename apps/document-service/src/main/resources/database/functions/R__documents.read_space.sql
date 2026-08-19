CREATE OR REPLACE FUNCTION documents.read_space(p_space_id BIGINT)
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
        RETURN QUERY SELECT s.id, s.owner_id, s.icon, s.name, s.description FROM documents.space s WHERE s.id = p_space_id;
    END;
$$