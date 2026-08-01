package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseTopicStorage
import ch.supsi.documentapplication.model.learn.Topic
import spock.lang.Subject

class DatabaseTopicStorageSpec extends DatabaseSpec {

    @Subject
    DatabaseTopicStorage storage

    def setup() {
        storage = new DatabaseTopicStorage(jdbcClient)
    }

    def "should create topic and read it back by page id"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long jobId = createBatchJobInstance()
        Long pageId = createPage(spaceId, jobId)
        def topic = new Topic(null, "notes.pdf", 1, 1, "Topic A", "Summary A", ["label-a", "label-b"])

        when:
        Long topicId = storage.create(topic, pageId)
        def topics = storage.getByPageId(pageId)

        then:
        topicId != null
        topics.size() == 1
        topics[0].id == topicId
        topics[0].title == "Topic A"
        topics[0].summary == "Summary A"
        topics[0].labels.sort() == ["label-a", "label-b"]
        topics[0].page == pageId
        topics[0].pageNumber == 1
    }

    def "should create topic and read it back by space id"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long jobId = createBatchJobInstance()
        Long pageId = createPage(spaceId, jobId)
        def topic = new Topic(null, "notes.pdf", 1, 1, "Topic B", "Summary B", ["space-label"])

        when:
        storage.create(topic, pageId)
        def topics = storage.getBySpaceId(spaceId)

        then:
        topics*.title.contains("Topic B")
        topics.find { it.title == "Topic B" }.summary == "Summary B"
        topics.find { it.title == "Topic B" }.labels == ["space-label"]
    }

    def "should return generated id from createAndReturnId"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long jobId = createBatchJobInstance()
        Long pageId = createPage(spaceId, jobId)
        def topic = new Topic(null, "notes.pdf", 1, 1, "Topic C", "Summary C", ["id-label"])

        when:
        Long firstId = storage.create(topic, pageId)
        Long secondId = storage.create(
                new Topic(null, "notes.pdf", 1, 1, "Topic D", "Summary D", ["id-label"]), pageId)

        then:
        firstId != null
        secondId != null
        firstId != secondId
    }

    def "findRandomTopicsBySpace returns at most the requested limit"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long jobId = createBatchJobInstance()
        Long pageId = createPage(spaceId, jobId)
        (1..4).each { idx ->
            storage.create(
                    new Topic(null, "notes.pdf", 1, 1, "Topic $idx", "Summary $idx", ["label"]),
                    pageId)
        }

        when:
        def topics = storage.findRandomTopicsBySpace(spaceId, 3)

        then:
        topics.size() == 3
        topics*.title.every { it.startsWith("Topic ") }
    }

    def "findRandomTopicsBySpace returns empty list for space without topics"() {
        given:
        createUser()
        Long spaceId = createSpace()

        when:
        def topics = storage.findRandomTopicsBySpace(spaceId, 3)

        then:
        topics.isEmpty()
    }
}
