CREATE OR REPLACE FUNCTION documents.delete_document(p_space_id BIGINT, p_document_id BIGINT)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
    BEGIN
        DELETE FROM documents.document d WHERE d.space_id = p_space_id AND d.id = p_document_id;
    END;
$$
