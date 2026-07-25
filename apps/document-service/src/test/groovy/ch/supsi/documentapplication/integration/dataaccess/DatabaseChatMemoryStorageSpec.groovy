package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseChatMemoryStorage
import spock.lang.Subject

import java.sql.Timestamp
import java.time.Instant

class DatabaseChatMemoryStorageSpec extends DatabaseSpec {

    @Subject
    DatabaseChatMemoryStorage storage

    def setup() {
        storage = new DatabaseChatMemoryStorage(jdbcClient)
    }

    def "should return timestamps ordered chronologically for a conversation"() {
        given:
        def conversationId = UUID.randomUUID().toString()
        def firstTimestamp = Instant.parse("2026-01-01T10:00:00Z")
        def secondTimestamp = Instant.parse("2026-01-01T10:01:00Z")
        insertChatMemoryRow(conversationId, "Hello", "USER", secondTimestamp)
        insertChatMemoryRow(conversationId, "Hi there", "ASSISTANT", firstTimestamp)

        when:
        def timestamps = storage.findTimestampsByConversationId(conversationId)

        then:
        timestamps == [firstTimestamp, secondTimestamp]
    }

    def "should return empty list when conversation has no messages"() {
        when:
        def timestamps = storage.findTimestampsByConversationId(UUID.randomUUID().toString())

        then:
        timestamps.isEmpty()
    }

    private void insertChatMemoryRow(String conversationId, String content, String type, Instant timestamp) {
        jdbcClient
                .sql("""
                        INSERT INTO spring_ai_chat_memory (conversation_id, content, type, "timestamp")
                        VALUES (:conversationId, :content, :type, :timestamp)
                        """)
                .param("conversationId", conversationId)
                .param("content", content)
                .param("type", type)
                .param("timestamp", Timestamp.from(timestamp))
                .update()
    }
}
