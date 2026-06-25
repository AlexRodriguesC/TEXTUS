CREATE OR REPLACE FUNCTION documents.delete_space(p_id BIGINT)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
    BEGIN
        DELETE FROM documents.space s WHERE s.id = p_id;
    END;
$$