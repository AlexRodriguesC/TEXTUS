package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseTopicStorage
import ch.supsi.documentapplication.model.learn.ExerciseType
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
        def topic = new Topic(null, "notes.pdf", 1, 1, "Topic A", "Summary A", ["label-a", "label-b"], [ExerciseType.QUESTION])

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
        topics[0].exerciseTypes == [ExerciseType.QUESTION]
        topics[0].page == pageId
        topics[0].pageNumber == 1
    }

    def "should create topic with multiple exercise types and read them back"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long jobId = createBatchJobInstance()
        Long pageId = createPage(spaceId, jobId)
        def topic = new Topic(
                null, "notes.pdf", 1, 1, "Water cycle", "Stages of the water cycle",
                ["hydrology"], [ExerciseType.LIST, ExerciseType.GROUP])

        when:
        Long topicId = storage.create(topic, pageId)
        def topics = storage.getByPageId(pageId)

        then:
        topicId != null
        topics.size() == 1
        topics[0].exerciseTypes.sort() == [ExerciseType.GROUP, ExerciseType.LIST]
    }

    def "should create topic and read it back by space id"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long jobId = createBatchJobInstance()
        Long pageId = createPage(spaceId, jobId)
        def topic = new Topic(null, "notes.pdf", 1, 1, "Topic B", "Summary B", ["space-label"], [ExerciseType.QUESTION])

        when:
        storage.create(topic, pageId)
        def topics = storage.getBySpaceId(spaceId)

        then:
        topics*.title.contains("Topic B")
        topics.find { it.title == "Topic B" }.summary == "Summary B"
        topics.find { it.title == "Topic B" }.labels == ["space-label"]
    }

    def "should read only topics from one document ordered by page then topic id"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long firstJobId = createBatchJobInstance("first-document")
        Long secondJobId = createBatchJobInstance("second-document")
        Long firstDocumentId = createDocument(spaceId, firstJobId, "first.pdf")
        Long secondDocumentId = createDocument(spaceId, secondJobId, "second.pdf")
        Long firstPageTwo = createPageForDocument(spaceId, firstDocumentId, 2)
        Long firstPageOne = createPageForDocument(spaceId, firstDocumentId, 1)
        Long secondPage = createPageForDocument(spaceId, secondDocumentId, 1)

        Long pageTwoTopic = storage.create(
                new Topic(null, "first.pdf", firstPageTwo, 2, "page-two", "summary", ["first"], [ExerciseType.QUESTION]),
                firstPageTwo)
        Long pageOneFirstTopic = storage.create(
                new Topic(null, "first.pdf", firstPageOne, 1, "page-one-first", "summary", ["first"], [ExerciseType.QUESTION]),
                firstPageOne)
        Long pageOneSecondTopic = storage.create(
                new Topic(null, "first.pdf", firstPageOne, 1, "page-one-second", "summary", ["first"], [ExerciseType.QUESTION]),
                firstPageOne)
        storage.create(
                new Topic(null, "second.pdf", secondPage, 1, "other-document", "summary", ["second"], [ExerciseType.QUESTION]),
                secondPage)

        when:
        def topics = storage.getByDocumentId(firstDocumentId)

        then:
        topics*.id == [pageOneFirstTopic, pageOneSecondTopic, pageTwoTopic]
        topics*.title == ["page-one-first", "page-one-second", "page-two"]
        topics*.pageNumber == [1, 1, 2]
    }

    def "should return generated id from createAndReturnId"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long jobId = createBatchJobInstance()
        Long pageId = createPage(spaceId, jobId)
        def topic = new Topic(null, "notes.pdf", 1, 1, "Topic C", "Summary C", ["id-label"], [ExerciseType.QUESTION])

        when:
        Long firstId = storage.create(topic, pageId)
        Long secondId = storage.create(
                new Topic(null, "notes.pdf", 1, 1, "Topic D", "Summary D", ["id-label"], [ExerciseType.QUESTION]), pageId)

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
                    new Topic(null, "notes.pdf", 1, 1, "Topic $idx", "Summary $idx", ["label"], [ExerciseType.QUESTION]),
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

    private Long createPageForDocument(Long spaceId, Long documentId, int pageNumber) {
        jdbcClient
                .sql("""
                INSERT INTO documents.page (space_id, document_id, page_number, title, summary)
                VALUES (:spaceId, :documentId, :pageNumber, 'title', 'summary')
                RETURNING id
                """)
                .param("spaceId", spaceId)
                .param("documentId", documentId)
                .param("pageNumber", pageNumber)
                .query(Long.class)
                .single()
    }
}
