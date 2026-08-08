CREATE OR REPLACE FUNCTION documents.move_document(
    p_space_id BIGINT,
    p_document_id BIGINT,
    p_order_index INTEGER
)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_document_count INTEGER;
BEGIN

    PERFORM 1
    FROM documents.document
    WHERE space_id = p_space_id
    FOR UPDATE;

    IF NOT EXISTS (
        SELECT 1
        FROM documents.document
        WHERE id = p_document_id
          AND space_id = p_space_id
    ) THEN
        RAISE EXCEPTION USING
            ERRCODE = 'P0002',
            MESSAGE = format('Document with id %s not found in space %s', p_document_id, p_space_id);
    END IF;

    SELECT COUNT(*)
    INTO v_document_count
    FROM documents.document
    WHERE space_id = p_space_id;

    IF p_order_index < 0 OR p_order_index >= v_document_count THEN
        RAISE EXCEPTION USING
            ERRCODE = '22023',
            MESSAGE = format(
                'Order index %s must be between 0 and %s',
                p_order_index,
                v_document_count - 1
            );
    END IF;

    WITH remaining_documents AS (
        SELECT id,
               ROW_NUMBER() OVER (ORDER BY order_index, id) - 1 AS remaining_index
        FROM documents.document
        WHERE space_id = p_space_id
          AND id <> p_document_id
    ),
    normalized_order AS (
        SELECT id,
               CASE
                   WHEN remaining_index >= p_order_index THEN remaining_index + 1
                   ELSE remaining_index
               END::INTEGER AS order_index
        FROM remaining_documents
        UNION ALL
        SELECT p_document_id, p_order_index
    )
    UPDATE documents.document document
    SET order_index = normalized_order.order_index
    FROM normalized_order
    WHERE document.id = normalized_order.id;
END;
$$;
