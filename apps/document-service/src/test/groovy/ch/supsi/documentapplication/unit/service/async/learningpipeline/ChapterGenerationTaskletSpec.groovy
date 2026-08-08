package ch.supsi.documentapplication.unit.service.async.learningpipeline

import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.dataaccess.IDatabaseAiUsageStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.IDatabasePathStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage
import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.model.ClientResult
import ch.supsi.documentapplication.model.Document
import ch.supsi.documentapplication.model.learn.Chapter
import ch.supsi.documentapplication.model.learn.LessonType
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.async.learningpipeline.ChapterConsolidator
import ch.supsi.documentapplication.service.async.learningpipeline.ChapterGenerationRequest
import ch.supsi.documentapplication.service.async.learningpipeline.ChapterGenerationTasklet
import ch.supsi.documentapplication.service.clients.OpenAIChapterClient
import ch.supsi.documentapplication.unit.service.async.TaskletTestSupport
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification
import spock.lang.Subject

class ChapterGenerationTaskletSpec extends Specification {

    IDatabaseTopicStorage databaseTopicStorage = Mock()
    IDatabaseDocumentStorage databaseDocumentStorage = Mock()
    IDatabasePathStorage databaseLearningStorage = Mock()
    OpenAIChapterClient openAIChapterClient = Mock()
    IDatabaseAiUsageStorage aiUsageStorage = Mock()
    AppProperties appProperties = new AppProperties()
    ChapterConsolidator chapterConsolidator = new ChapterConsolidator()
    StepContribution stepContribution = Mock()

    @Subject
    ChapterGenerationTasklet tasklet = new ChapterGenerationTasklet(
            databaseDocumentStorage,
            databaseTopicStorage,
            databaseLearningStorage,
            openAIChapterClient,
            aiUsageStorage,
            appProperties,
            chapterConsolidator)

    Long spaceId = 7L
    Long pathId = 42L
    List<Chapter> persisted = []

    def setup() {
        appProperties.maxChapters = 10
        ReflectionTestUtils.setField(tasklet, "spaceId", spaceId)
        persisted.clear()
    }

    def "should compute the dynamic chapter cap from topic count"() {
        expect:
        tasklet.computeMaxChapters(topicCount) == expected

        where:
        topicCount | expected
        0          | 1
        2          | 1
        3          | 1
        8          | 2
        30         | 10
        33         | 10
    }

    def "should pass the absolute ceiling when the topic-derived cap is higher"() {
        given:
        appProperties.maxChapters = 4

        expect:
        tasklet.computeMaxChapters(30) == 4
    }

    def "should generate chapters per document and persist them in document and page order"() {
        given:
        def firstDocumentTopics = [
                topic(1L, 5, ["alpha"]),
                topic(2L, 6, ["alpha"]),
                topic(3L, 7, ["alpha"]),
                topic(4L, 1, ["beta"]),
                topic(5L, 2, ["beta"]),
                topic(6L, 3, ["beta"])
        ]
        def secondDocumentTopics = [
                topic(7L, 2, ["gamma"]),
                topic(8L, 3, ["gamma"]),
                topic(9L, 4, ["gamma"])
        ]
        def firstDocumentChapters = [
                new Chapter(0, "Alpha", "Alpha chapter focus here", [0L]),
                new Chapter(1, "Beta", "Beta chapter focus here", [1L])
        ]
        def secondDocumentChapters = [
                new Chapter(0, "Gamma", "Gamma chapter focus here", [0L])
        ]
        def chunkContext = TaskletTestSupport.chunkContext(["PATH_ID": pathId])
        def usages = [
                new AiUsage(5, 7, "provider", "model"),
                new AiUsage(3, 4, "provider", "model")
        ]
        List<ChapterGenerationRequest> capturedRequests = []

        when:
        RepeatStatus status = tasklet.execute(stepContribution, chunkContext)

        then:
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [
                new Document("first.pdf", 11L, 101L, 0),
                new Document("second.pdf", 22L, 102L, 1)
        ]
        1 * databaseTopicStorage.getByDocumentId(11L) >> firstDocumentTopics
        1 * databaseTopicStorage.getByDocumentId(22L) >> secondDocumentTopics
        2 * openAIChapterClient.getResult(_ as ChapterGenerationRequest) >> { ChapterGenerationRequest request ->
            capturedRequests << request
            capturedRequests.size() == 1
                    ? new ClientResult<>(firstDocumentChapters, [usages[0]])
                    : new ClientResult<>(secondDocumentChapters, [usages[1]])
        }
        1 * aiUsageStorage.create("learning_path", pathId, "chapter_generate", usages[0])
        1 * aiUsageStorage.create("learning_path", pathId, "chapter_generate", usages[1])
        3 * databaseLearningStorage.create(pathId, _ as Chapter, spaceId) >> { Long p, Chapter chapter, Long s ->
            persisted << chapter
        }
        status == RepeatStatus.FINISHED
        capturedRequests*.labels*.name == [["alpha", "beta"], ["gamma"]]
        persisted*.title == ["Beta", "Alpha", "Gamma"]
        persisted*.index == [0, 1, 2]
        persisted*.documentId == [11L, 11L, 22L]
        persisted.every { it.topics.size() >= 3 }
        persisted.every { it.lessons != null && !it.lessons.isEmpty() }
        persisted[0].topics == [4L, 5L, 6L]
        persisted[1].topics == [1L, 2L, 3L]
        persisted[2].topics == [7L, 8L, 9L]
    }

    def "should order consolidated chapters by their earliest assigned page"() {
        given:
        def topics = [
                topic(1L, 9, ["alpha"]),
                topic(2L, 10, ["alpha"]),
                topic(3L, 11, ["alpha"]),
                topic(4L, 8, ["beta"]),
                topic(5L, 12, ["beta"]),
                topic(6L, 1, ["gamma"]),
                topic(7L, 2, ["gamma"]),
                topic(8L, 3, ["gamma"])
        ]
        def aiChapters = [
                new Chapter(0, "Alpha", "Alpha chapter focus here", [0L]),
                new Chapter(1, "Beta", "Beta chapter focus here", [1L]),
                new Chapter(2, "Gamma", "Gamma chapter focus here", [2L])
        ]

        when:
        tasklet.execute(stepContribution, TaskletTestSupport.chunkContext(["PATH_ID": pathId]))

        then:
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [new Document("one.pdf", 11L, 101L, 0)]
        1 * databaseTopicStorage.getByDocumentId(11L) >> topics
        1 * openAIChapterClient.getResult(_ as ChapterGenerationRequest) >> new ClientResult<>(aiChapters, [])
        2 * databaseLearningStorage.create(pathId, _ as Chapter, spaceId) >> { Long p, Chapter chapter, Long s ->
            persisted << chapter
        }
        persisted*.title == ["Gamma", "Alpha"]
        persisted*.topics == [[6L, 7L, 8L], [4L, 1L, 2L, 3L, 5L]]
        persisted*.index == [0, 1]
    }

    def "should build contiguous lessons that follow page order after consolidation"() {
        given:
        def topics = [
                topic(1L, 10, ["alpha"]),
                topic(2L, 11, ["alpha"]),
                topic(3L, 12, ["alpha"]),
                topic(4L, 13, ["alpha"]),
                topic(5L, 9, ["beta"]),
                topic(6L, 14, ["beta"]),
                topic(7L, 1, ["gamma"]),
                topic(8L, 2, ["gamma"]),
                topic(9L, 3, ["gamma"]),
                topic(10L, 4, ["gamma"]),
                topic(11L, 5, ["gamma"])
        ]
        def aiChapters = [
                new Chapter(0, "Alpha", "Alpha chapter focus here", [0L]),
                new Chapter(1, "Beta", "Beta chapter focus here", [1L]),
                new Chapter(2, "Gamma", "Gamma chapter focus here", [2L])
        ]

        when:
        tasklet.execute(stepContribution, TaskletTestSupport.chunkContext(["PATH_ID": pathId]))

        then:
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [new Document("one.pdf", 11L, 101L, 0)]
        1 * databaseTopicStorage.getByDocumentId(11L) >> topics
        1 * openAIChapterClient.getResult(_ as ChapterGenerationRequest) >> new ClientResult<>(aiChapters, [])
        2 * databaseLearningStorage.create(pathId, _ as Chapter, spaceId) >> { Long p, Chapter chapter, Long s ->
            persisted << chapter
        }
        persisted*.title == ["Gamma", "Alpha"]
        persisted[0].topics == [7L, 8L, 9L, 10L, 11L]
        persisted[0].lessons[0].topics == [7L, 8L, 9L, 10L, 11L]
        persisted[1].topics == [5L, 1L, 2L, 3L, 4L, 6L]
        persisted[1].lessons[0].topics == [5L, 1L, 2L]
        persisted[1].lessons[1].topics == [3L, 4L, 6L]
        persisted[1].lessons[2].type == LessonType.CHECKPOINT
    }

    def "should use consolidated chapter index to break equal earliest page ties"() {
        given:
        def topics = [
                topic(1L, 1, ["alpha"]),
                topic(2L, 2, ["alpha"]),
                topic(3L, 3, ["alpha"]),
                topic(4L, 1, ["beta"]),
                topic(5L, 4, ["beta"]),
                topic(6L, 5, ["beta"])
        ]
        def aiChapters = [
                new Chapter(0, "Alpha", "Alpha chapter focus here", [0L]),
                new Chapter(1, "Beta", "Beta chapter focus here", [1L])
        ]

        when:
        tasklet.execute(stepContribution, TaskletTestSupport.chunkContext(["PATH_ID": pathId]))

        then:
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [new Document("one.pdf", 11L, 101L, 0)]
        1 * databaseTopicStorage.getByDocumentId(11L) >> topics
        1 * openAIChapterClient.getResult(_ as ChapterGenerationRequest) >> new ClientResult<>(aiChapters, [])
        2 * databaseLearningStorage.create(pathId, _ as Chapter, spaceId) >> { Long p, Chapter chapter, Long s ->
            persisted << chapter
        }
        persisted*.title == ["Alpha", "Beta"]
        persisted*.index == [0, 1]
    }

    def "should fail before calling the LLM when a document has no topics"() {
        when:
        tasklet.execute(stepContribution, TaskletTestSupport.chunkContext(["PATH_ID": pathId]))

        then:
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [new Document("empty.pdf", 11L, 101L, 0)]
        1 * databaseTopicStorage.getByDocumentId(11L) >> []
        0 * openAIChapterClient._
        0 * databaseLearningStorage._
        def exception = thrown(IllegalStateException)
        exception.message == "Document with id 11 has no topics; chapter generation cannot continue."
    }

    def "should keep a single chapter when fewer than three topics exist"() {
        given:
        def topics = [
                topic(1L, ["alpha"]),
                topic(2L, ["beta"])
        ]
        def aiChapters = [
                new Chapter(0, "Alpha", "Alpha chapter focus here", [0L]),
                new Chapter(1, "Beta", "Beta chapter focus here", [1L])
        ]
        def chunkContext = TaskletTestSupport.chunkContext(["PATH_ID": pathId])

        when:
        RepeatStatus status = tasklet.execute(stepContribution, chunkContext)

        then:
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [new Document("one.pdf", 11L, 101L, 0)]
        1 * databaseTopicStorage.getByDocumentId(11L) >> topics
        1 * openAIChapterClient.getResult({ ChapterGenerationRequest request ->
            request.maxChapters == 1
        }) >> new ClientResult<>(aiChapters, [])
        1 * databaseLearningStorage.create(pathId, _ as Chapter, spaceId) >> { Long p, Chapter chapter, Long s ->
            persisted << chapter
        }
        status == RepeatStatus.FINISHED
        persisted.size() == 1
        persisted[0].index == 0
        persisted[0].documentId == 11L
        persisted[0].topics == [1L, 2L]
        persisted[0].lessons != null
    }

    def "should persist chapter usage for generate and feedback operations"() {
        given:
        def topics = [
                topic(1L, ["alpha"]),
                topic(2L, ["alpha"]),
                topic(3L, ["alpha"])
        ]
        def aiChapters = [
                new Chapter(0, "Alpha", "Alpha chapter focus here", [0L])
        ]
        def chunkContext = TaskletTestSupport.chunkContext(["PATH_ID": pathId])
        def usages = [
                new AiUsage(1, 2, "provider", "model"),
                new AiUsage(3, 4, "provider", "model")
        ]

        when:
        tasklet.execute(stepContribution, chunkContext)

        then:
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [new Document("one.pdf", 11L, 101L, 0)]
        1 * databaseTopicStorage.getByDocumentId(11L) >> topics
        1 * openAIChapterClient.getResult(_ as ChapterGenerationRequest) >> new ClientResult<>(aiChapters, usages)
        1 * aiUsageStorage.create("learning_path", pathId, "chapter_generate", usages[0])
        1 * aiUsageStorage.create("learning_path", pathId, "chapter_feedback", usages[1])
        1 * databaseLearningStorage.create(pathId, _ as Chapter, spaceId)
    }

    private static Topic topic(Long id, List<String> labels) {
        topic(id, 1, labels)
    }

    private static Topic topic(Long id, int pageNumber, List<String> labels) {
        new Topic(id, null, null, pageNumber, "topic-$id", "summary-$id", labels)
    }
}
