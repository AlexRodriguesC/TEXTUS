CREATE OR REPLACE FUNCTION documents.update_mind_map_status(p_space_id BIGINT, p_status mind_map_status)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
    BEGIN
        -- Validate space has a mind map
        IF NOT EXISTS (
            SELECT 1
            FROM documents.space s
            WHERE s.id = p_space_id
              AND s.mind_map_id IS NOT NULL
        ) THEN
            RAISE EXCEPTION 'Mind map associated to space with id % not found', p_space_id;
        END IF;

        UPDATE documents.mind_map
        SET status = p_status
        WHERE id = (
            SELECT s.mind_map_id
            FROM documents.space s
            WHERE s.id = p_space_id
        );
    END;
$$
