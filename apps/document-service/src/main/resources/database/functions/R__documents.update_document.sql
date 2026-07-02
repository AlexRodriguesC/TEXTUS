CREATE OR REPLACE FUNCTION documents.update_document(p_document_id BIGINT, p_job_id BIGINT)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
    BEGIN
        UPDATE documents.document SET job_id = p_job_id WHERE id = p_document_id;
    END;
$$
