CREATE TABLE documents.chat_message_topic
(
    chat_id           UUID        NOT NULL REFERENCES documents.chat (id) ON DELETE CASCADE,
    message_timestamp TIMESTAMPTZ NOT NULL,
    topic_id          BIGINT      NOT NULL REFERENCES documents.topic (id) ON DELETE CASCADE,
    PRIMARY KEY (chat_id, message_timestamp, topic_id)
);

CREATE INDEX chat_message_topic_chat_idx ON documents.chat_message_topic (chat_id);
