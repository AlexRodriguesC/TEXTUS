CREATE OR REPLACE FUNCTION documents.create_document(p_space_id BIGINT, p_job_id BIGINT)
    RETURNS BIGINT
    LANGUAGE plpgsql
AS
$$
    DECLARE
        v_document_id BIGINT;
    BEGIN
        -- Validate space exists
        IF NOT EXISTS (
            SELECT 1 FROM documents.space WHERE id = p_space_id
        ) THEN
            RAISE EXCEPTION 'Space with id % not found', p_space_id;
        END IF;

        INSERT INTO documents.document(space_id, job_id) VALUES (p_space_id, p_job_id) RETURNING id INTO v_document_id;

        RETURN v_document_id;
    END;
$$
