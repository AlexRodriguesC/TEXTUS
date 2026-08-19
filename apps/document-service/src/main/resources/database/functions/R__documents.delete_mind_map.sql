CREATE OR REPLACE FUNCTION documents.delete_mind_map(p_space_id BIGINT)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_mind_map_id BIGINT;
BEGIN
    SELECT s.mind_map_id INTO v_mind_map_id FROM documents.space s WHERE s.id = p_space_id;

    UPDATE documents.space SET mind_map_id = NULL WHERE id = p_space_id;

    IF v_mind_map_id IS NOT NULL THEN
        DELETE FROM documents.mind_map WHERE id = v_mind_map_id;
    END IF;
END;
$$;
