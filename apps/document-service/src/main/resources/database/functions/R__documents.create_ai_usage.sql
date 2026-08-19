CREATE OR REPLACE FUNCTION documents.create_ai_usage(
    p_entity_type TEXT,
    p_entity_id   BIGINT,
    p_operation   TEXT,
    p_tokens_in   INTEGER,
    p_tokens_out  INTEGER,
    p_provider    TEXT,
    p_model       TEXT
)
    RETURNS BIGINT
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_usage_id BIGINT;
BEGIN
    INSERT INTO documents.ai_usage (
        entity_type,
        entity_id,
        operation,
        tokens_in,
        tokens_out,
        provider,
        model
    )
    VALUES (
        p_entity_type,
        p_entity_id,
        p_operation,
        p_tokens_in,
        p_tokens_out,
        p_provider,
        p_model
    )
    RETURNING id INTO v_usage_id;

    RETURN v_usage_id;
END;
$$;
