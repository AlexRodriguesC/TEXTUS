package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseChatMessageTopicStorage
import ch.supsi.documentapplication.dataaccess.DatabaseChatStorage
import ch.supsi.documentapplication.dataaccess.DatabaseTopicStorage
import ch.supsi.documentapplication.model.learn.Topic
import spock.lang.Subject

import java.time.Instant

class DatabaseChatMessageTopicStorageSpec extends DatabaseSpec {

    @Subject
    DatabaseChatMessageTopicStorage storage

    DatabaseChatStorage chatStorage
    DatabaseTopicStorage topicStorage

    def setup() {
        storage = new DatabaseChatMessageTopicStorage(jdbcClient)
        chatStorage = new DatabaseChatStorage(jdbcClient)
        topicStorage = new DatabaseTopicStorage(jdbcClient)
    }

    def "should save and load topic ids grouped by message timestamp"() {
        given:
        createUser("grounding-owner")
        Long spaceId = createSpace("grounding-owner")
        UUID chatId = chatStorage.create(spaceId, "grounding-owner", "Grounding chat")
        Long jobId = createBatchJobInstance()
        Long pageId = createPage(spaceId, jobId)
        Long topicA = topicStorage.create(new Topic(null, "notes.pdf", 1, "Topic A", "Summary A", []), pageId)
        Long topicB = topicStorage.create(new Topic(null, "notes.pdf", 1, "Topic B", "Summary B", []), pageId)
        def firstTimestamp = Instant.parse("2026-01-01T10:00:00Z")
        def secondTimestamp = Instant.parse("2026-01-01T10:01:00Z")

        when:
        storage.save(chatId, firstTimestamp, [topicB, topicA])
        storage.save(chatId, secondTimestamp, [topicA])
        def topicsByTimestamp = storage.findByChatId(chatId)

        then:
        topicsByTimestamp.size() == 2
        topicsByTimestamp[firstTimestamp] == [topicA, topicB]
        topicsByTimestamp[secondTimestamp] == [topicA]
    }

    def "should not insert rows when topic ids are empty"() {
        given:
        createUser("grounding-owner-2")
        Long spaceId = createSpace("grounding-owner-2")
        UUID chatId = chatStorage.create(spaceId, "grounding-owner-2", "Empty grounding chat")
        def timestamp = Instant.parse("2026-01-01T11:00:00Z")

        when:
        storage.save(chatId, timestamp, [])
        def topicsByTimestamp = storage.findByChatId(chatId)

        then:
        topicsByTimestamp.isEmpty()
    }
}
