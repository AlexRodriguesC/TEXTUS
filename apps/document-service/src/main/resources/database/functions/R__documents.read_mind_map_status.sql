CREATE OR REPLACE FUNCTION documents.read_mind_map_status(p_space_id BIGINT)
    RETURNS mind_map_status
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_status mind_map_status;
BEGIN
    SELECT m.status
    INTO v_status
    FROM documents.space s
             JOIN documents.mind_map m ON s.mind_map_id = m.id
    WHERE s.id = p_space_id;

    IF v_status IS NULL THEN
        RAISE EXCEPTION 'Mind map associated to space with id % not found', p_space_id;
    END IF;

    RETURN v_status;
END;
$$;