package ch.supsi.documentapplication.dataaccess;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class DatabaseChatMessageTopicStorage extends DatabaseStorage implements IDatabaseChatMessageTopicStorage {

    private static final String CHAT_ID = "chat_id";
    private static final String MESSAGE_TIMESTAMP = "message_timestamp";
    private static final String TOPIC_ID = "topic_id";

    @Autowired
    public DatabaseChatMessageTopicStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public void save(UUID chatId, Instant messageTimestamp, List<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            return;
        }
        for (Long topicId : topicIds) {
            jdbcClient
                    .sql(
                            """
                            INSERT INTO documents.chat_message_topic (chat_id, message_timestamp, topic_id)
                            VALUES (:chatId, :messageTimestamp, :topicId)
                            ON CONFLICT DO NOTHING
                            """)
                    .param(CHAT_ID, chatId)
                    .param(MESSAGE_TIMESTAMP, messageTimestamp)
                    .param(TOPIC_ID, topicId)
                    .update();
        }
    }

    @Override
    public Map<Instant, List<Long>> findByChatId(UUID chatId) {
        return jdbcClient
                .sql(
                        """
                        SELECT message_timestamp, topic_id
                        FROM documents.chat_message_topic
                        WHERE chat_id = :chatId
                        ORDER BY message_timestamp, topic_id
                        """)
                .param(CHAT_ID, chatId)
                .query((rs, rowNum) ->
                        new TopicRow(rs.getTimestamp(MESSAGE_TIMESTAMP).toInstant(), rs.getLong(TOPIC_ID)))
                .list()
                .stream()
                .collect(
                        LinkedHashMap::new,
                        (map, row) -> map.computeIfAbsent(row.messageTimestamp(), ignored -> new ArrayList<>())
                                .add(row.topicId()),
                        Map::putAll);
    }

    private record TopicRow(Instant messageTimestamp, Long topicId) {}
}
