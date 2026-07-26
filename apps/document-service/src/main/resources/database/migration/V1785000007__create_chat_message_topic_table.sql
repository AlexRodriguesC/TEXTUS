CREATE TABLE documents.chat_message_topic
(
    chat_id             UUID   NOT NULL REFERENCES documents.chat (id) ON DELETE CASCADE,
    message_sequence_id BIGINT NOT NULL,
    topic_id            BIGINT NOT NULL REFERENCES documents.topic (id) ON DELETE CASCADE,
    PRIMARY KEY (chat_id, message_sequence_id, topic_id)
);

CREATE INDEX chat_message_topic_chat_idx ON documents.chat_message_topic (chat_id);
