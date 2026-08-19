CREATE OR REPLACE FUNCTION documents.save_chat_message_topics(
    p_chat_id             UUID,
    p_message_sequence_id BIGINT,
    p_topic_ids           BIGINT[]
)
    RETURNS VOID
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_topic_id BIGINT;
BEGIN
    IF p_topic_ids IS NULL OR cardinality(p_topic_ids) = 0 THEN
        RETURN;
    END IF;

    FOREACH v_topic_id IN ARRAY p_topic_ids
        LOOP
            INSERT INTO documents.chat_message_topic (chat_id, message_sequence_id, topic_id)
            VALUES (p_chat_id, p_message_sequence_id, v_topic_id)
            ON CONFLICT DO NOTHING;
        END LOOP;
END;
$$;
