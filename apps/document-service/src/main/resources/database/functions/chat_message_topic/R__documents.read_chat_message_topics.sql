CREATE OR REPLACE FUNCTION documents.read_chat_message_topics(
    p_chat_id UUID
)
    RETURNS TABLE (
        message_sequence_id BIGINT,
        topic_id            BIGINT
    )
    LANGUAGE sql
AS
$$
SELECT cmt.message_sequence_id,
       cmt.topic_id
FROM documents.chat_message_topic cmt
WHERE cmt.chat_id = p_chat_id
ORDER BY cmt.message_sequence_id, cmt.topic_id;
$$;
