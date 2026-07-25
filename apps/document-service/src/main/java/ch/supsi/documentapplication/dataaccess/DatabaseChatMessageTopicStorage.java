package ch.supsi.documentapplication.dataaccess;

import java.sql.Timestamp;
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

    private static final String CHAT_ID = "chatId";
    private static final String MESSAGE_TIMESTAMP = "messageTimestamp";
    private static final String TOPIC_IDS = "topicIds";

    @Autowired
    public DatabaseChatMessageTopicStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public void save(UUID chatId, Instant messageTimestamp, List<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            return;
        }
        jdbcClient
                .sql("SELECT documents.save_chat_message_topics(:chatId, :messageTimestamp, :topicIds)")
                .param(CHAT_ID, chatId)
                .param(MESSAGE_TIMESTAMP, Timestamp.from(messageTimestamp))
                .param(TOPIC_IDS, topicIds.toArray(new Long[0]))
                .query()
                .singleValue();
    }

    @Override
    public Map<Instant, List<Long>> findByChatId(UUID chatId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_chat_message_topics(:chatId)")
                .param(CHAT_ID, chatId)
                .query((rs, rowNum) ->
                        new TopicRow(rs.getTimestamp("message_timestamp").toInstant(), rs.getLong("topic_id")))
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
