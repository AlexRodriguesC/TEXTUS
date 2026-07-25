package ch.supsi.documentapplication.dataaccess;

import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class DatabaseChatMemoryStorage extends DatabaseStorage implements IDatabaseChatMemoryStorage {

    private static final String CONVERSATION_ID = "conversationId";

    public DatabaseChatMemoryStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public List<Instant> findTimestampsByConversationId(String conversationId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_chat_memory_timestamps(:conversationId)")
                .param(CONVERSATION_ID, conversationId)
                .query((rs, rowNum) -> rs.getTimestamp("message_timestamp").toInstant())
                .list();
    }
}
