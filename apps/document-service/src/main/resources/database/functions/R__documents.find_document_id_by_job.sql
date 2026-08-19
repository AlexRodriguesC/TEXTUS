CREATE OR REPLACE FUNCTION documents.find_document_id_by_job(p_job_id BIGINT)
    RETURNS BIGINT
    LANGUAGE sql
AS
$$
SELECT d.id
FROM documents.document d
WHERE d.job_id = p_job_id
LIMIT 1;
$$;
