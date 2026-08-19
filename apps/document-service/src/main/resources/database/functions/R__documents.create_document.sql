CREATE OR REPLACE FUNCTION documents.create_document(p_space_id BIGINT, p_job_id BIGINT, p_name TEXT)
    RETURNS BIGINT
    LANGUAGE plpgsql
AS
$$
    DECLARE
        v_document_id BIGINT;
        v_order_index INTEGER;
    BEGIN
        PERFORM 1
        FROM documents.space
        WHERE id = p_space_id;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'Space with id % not found', p_space_id;
        END IF;

        SELECT COALESCE(MAX(d.order_index), -1) + 1
        INTO v_order_index
        FROM documents.document d
        WHERE d.space_id = p_space_id;

        INSERT INTO documents.document(space_id, job_id, name, order_index)
        VALUES (p_space_id, p_job_id, p_name, v_order_index)
        RETURNING id INTO v_document_id;

        RETURN v_document_id;
    END;
$$
