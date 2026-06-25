CREATE OR REPLACE FUNCTION documents.update_document_status(p_id BIGINT, p_status document_status)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
    BEGIN
        UPDATE documents.document d SET status = p_status WHERE id = p_id;
    END;
$$
