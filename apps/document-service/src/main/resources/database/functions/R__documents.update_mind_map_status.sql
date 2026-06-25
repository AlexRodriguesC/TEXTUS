CREATE OR REPLACE FUNCTION documents.update_mind_map_status(p_space_id BIGINT, p_status mind_map_status)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
    BEGIN
        -- Validate space exists
        IF NOT EXISTS (
            SELECT 1 FROM documents.mind_map WHERE space_id = p_space_id
        ) THEN
            RAISE EXCEPTION 'Mind map associated to space with id % not found', p_space_id;
        END IF;

        UPDATE documents.mind_map SET status = p_status WHERE space_id = p_space_id;
    END;
$$
