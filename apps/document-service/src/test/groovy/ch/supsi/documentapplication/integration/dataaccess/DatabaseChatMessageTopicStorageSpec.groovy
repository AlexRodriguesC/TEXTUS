package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseChatMessageTopicStorage
import ch.supsi.documentapplication.dataaccess.DatabaseChatStorage
import ch.supsi.documentapplication.dataaccess.DatabaseTopicStorage
import ch.supsi.documentapplication.model.learn.ExerciseType
import ch.supsi.documentapplication.model.learn.Topic
import spock.lang.Subject

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

    def "should save and load topic ids grouped by message sequence id"() {
        given:
        createUser("grounding-owner")
        Long spaceId = createSpace("grounding-owner")
        UUID chatId = chatStorage.create(spaceId, "grounding-owner", "Grounding chat")
        Long jobId = createBatchJobInstance()
        Long pageId = createPage(spaceId, jobId)
        Long topicA = topicStorage.create(new Topic(null, "notes.pdf", 1, 1, "Topic A", "Summary A", [], [ExerciseType.QUESTION]), pageId)
        Long topicB = topicStorage.create(new Topic(null, "notes.pdf", 1, 1, "Topic B", "Summary B", [], [ExerciseType.QUESTION]), pageId)

        when:
        storage.save(chatId, 1L, [topicB, topicA])
        storage.save(chatId, 3L, [topicA])
        def topicsBySequenceId = storage.findByChatId(chatId)

        then:
        topicsBySequenceId.size() == 2
        topicsBySequenceId[1L] == [topicA, topicB]
        topicsBySequenceId[3L] == [topicA]
    }

    def "should not insert rows when topic ids are empty"() {
        given:
        createUser("grounding-owner-2")
        Long spaceId = createSpace("grounding-owner-2")
        UUID chatId = chatStorage.create(spaceId, "grounding-owner-2", "Empty grounding chat")

        when:
        storage.save(chatId, 0L, [])
        def topicsBySequenceId = storage.findByChatId(chatId)

        then:
        topicsBySequenceId.isEmpty()
    }

    def "should retain topic associations keyed by sequence id across memory re-save"() {
        given:
        createUser("grounding-owner-3")
        Long spaceId = createSpace("grounding-owner-3")
        UUID chatId = chatStorage.create(spaceId, "grounding-owner-3", "Multi-turn chat")
        Long jobId = createBatchJobInstance()
        Long pageId = createPage(spaceId, jobId)
        Long topicA = topicStorage.create(new Topic(null, "notes.pdf", 1, 1, "Topic A", "Summary A", [], [ExerciseType.QUESTION]), pageId)

        when:
        storage.save(chatId, 1L, [topicA])
        def topicsAfterFirstTurn = storage.findByChatId(chatId)

        then:
        topicsAfterFirstTurn[1L] == [topicA]

        when:
        storage.save(chatId, 3L, [topicA])
        def topicsAfterSecondTurn = storage.findByChatId(chatId)

        then:
        topicsAfterSecondTurn[1L] == [topicA]
        topicsAfterSecondTurn[3L] == [topicA]
    }
}
