CREATE OR REPLACE FUNCTION documents.create_mind_map(p_space_id BIGINT, p_job_id BIGINT)
    RETURNS BIGINT
    LANGUAGE plpgsql
AS
$$
    DECLARE
        v_mind_map_id BIGINT;
    BEGIN
        -- Validate space exists
        IF NOT EXISTS (
            SELECT 1 FROM documents.space WHERE id = p_space_id
        ) THEN
            RAISE EXCEPTION 'Space with id % not found', p_space_id;
        END IF;

        INSERT INTO documents.mind_map(job_id) VALUES (p_job_id) RETURNING id INTO v_mind_map_id;
        UPDATE documents.space SET mind_map_id = v_mind_map_id WHERE id = p_space_id;

        RETURN v_mind_map_id;
    END;
$$
