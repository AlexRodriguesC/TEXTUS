CREATE OR REPLACE FUNCTION documents.delete_learning_path(p_space_id BIGINT)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
    BEGIN
        DELETE FROM documents.learning_path lp WHERE lp.space_id = p_space_id;
    END;
$$
