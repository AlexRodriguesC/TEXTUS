package ch.supsi.documentapplication.unit.service.async.learningpipeline

import ch.supsi.documentapplication.dataaccess.IDatabaseAiUsageStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.IDatabasePathStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage
import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.model.ClientResult
import ch.supsi.documentapplication.model.Document
import ch.supsi.documentapplication.model.learn.Chapter
import ch.supsi.documentapplication.model.learn.LessonType
import ch.supsi.documentapplication.model.learn.ExerciseType
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.async.learningpipeline.ChapterBoundary
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
    StepContribution stepContribution = Mock()

    @Subject
    ChapterGenerationTasklet tasklet = new ChapterGenerationTasklet(
            databaseDocumentStorage,
            databaseTopicStorage,
            databaseLearningStorage,
            openAIChapterClient,
            aiUsageStorage)

    Long spaceId = 7L
    Long pathId = 42L
    List<Chapter> persisted = []
    List<ChapterGenerationRequest> capturedRequests = []

    def setup() {
        ReflectionTestUtils.setField(tasklet, "spaceId", spaceId)
        persisted.clear()
        capturedRequests.clear()
    }

    def "should sort shuffled topics by page and topic id and join labels without summaries"() {
        given:
        def topics = [
                topic(3L, 2, ["kitchen", "safety"]),
                topic(1L, 1, ["home", "cooking", "chicken"]),
                topic(2L, 1, ["basics"])
        ]

        when:
        tasklet.execute(stepContribution, TaskletTestSupport.chunkContext(["PATH_ID": pathId]))

        then:
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [new Document("one.pdf", 11L, 101L, 0)]
        1 * databaseTopicStorage.getByDocumentId(11L) >> topics
        1 * openAIChapterClient.getResult(_ as ChapterGenerationRequest) >> { ChapterGenerationRequest request ->
            capturedRequests << request
            new ClientResult<>(new ChapterBoundary("Home Cooking", "Introduces home cooking basics", 3), [])
        }
        1 * databaseLearningStorage.create(pathId, _ as Chapter, spaceId) >> { Long p, Chapter chapter, Long s ->
            persisted << chapter
        }
        capturedRequests.size() == 1
        capturedRequests[0].topics()*.position() == [1, 2, 3]
        capturedRequests[0].topics()*.labels() == ["home cooking chicken", "basics", "kitchen safety"]
        persisted[0].topics == [1L, 2L, 3L]
        !capturedRequests[0].topics().any { it.labels().contains("summary") }
    }

    def "should process multi-window carry-over with exact once assignment and no gaps"() {
        given:
        def topics = (1..230).collect { int i ->
            topic((long) i, i, ["topic$i"])
        }
        def usages = [
                new AiUsage(1, 2, "provider", "model"),
                new AiUsage(3, 4, "provider", "model"),
                new AiUsage(5, 6, "provider", "model")
        ]

        when:
        RepeatStatus status = tasklet.execute(stepContribution, TaskletTestSupport.chunkContext(["PATH_ID": pathId]))

        then:
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [new Document("one.pdf", 11L, 101L, 0)]
        1 * databaseTopicStorage.getByDocumentId(11L) >> topics
        3 * openAIChapterClient.getResult(_ as ChapterGenerationRequest) >> { ChapterGenerationRequest request ->
            capturedRequests << request
            int callIndex = capturedRequests.size()
            int cut = callIndex == 1 ? 40 : (callIndex == 2 ? 100 : 90)
            new ClientResult<>(
                    new ChapterBoundary("Chapter $callIndex", "Chapter focus number $callIndex here", cut),
                    [usages[callIndex - 1]])
        }
        1 * aiUsageStorage.create("learning_path", pathId, "chapter_generate", usages[0])
        1 * aiUsageStorage.create("learning_path", pathId, "chapter_generate", usages[1])
        1 * aiUsageStorage.create("learning_path", pathId, "chapter_generate", usages[2])
        3 * databaseLearningStorage.create(pathId, _ as Chapter, spaceId) >> { Long p, Chapter chapter, Long s ->
            persisted << chapter
        }
        status == RepeatStatus.FINISHED
        capturedRequests*.topics()*.size() == [100, 100, 90]
        capturedRequests[0].topics()*.position() == (1..100).toList()
        capturedRequests[1].topics()*.position() == (1..100).toList()
        capturedRequests[1].topics().first().labels() == "topic41"
        capturedRequests[2].topics().first().labels() == "topic141"
        persisted*.index == [0, 1, 2]
        persisted*.topics*.size() == [40, 100, 90]
        persisted[0].topics == (1L..40L).toList()
        persisted[1].topics == (41L..140L).toList()
        persisted[2].topics == (141L..230L).toList()
        def allAssigned = persisted.collectMany { it.topics }
        allAssigned == (1L..230L).toList()
        allAssigned.toSet().size() == 230
    }

    def "should use a single window when fewer than 100 topics exist"() {
        given:
        def topics = (1..12).collect { int i -> topic((long) i, i, ["label$i"]) }

        when:
        tasklet.execute(stepContribution, TaskletTestSupport.chunkContext(["PATH_ID": pathId]))

        then:
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [new Document("one.pdf", 11L, 101L, 0)]
        1 * databaseTopicStorage.getByDocumentId(11L) >> topics
        1 * openAIChapterClient.getResult(_ as ChapterGenerationRequest) >> { ChapterGenerationRequest request ->
            capturedRequests << request
            new ClientResult<>(new ChapterBoundary("Single Window", "Covers the entire document topics", 12), [])
        }
        1 * databaseLearningStorage.create(pathId, _ as Chapter, spaceId) >> { Long p, Chapter chapter, Long s ->
            persisted << chapter
        }
        capturedRequests.size() == 1
        capturedRequests[0].topics().size() == 12
        persisted.size() == 1
        persisted[0].topics == (1L..12L).toList()
    }

    def "should accept a final partial window smaller than the minimum without retrying"() {
        given:
        def topics = (1..105).collect { int i -> topic((long) i, i, ["label$i"]) }

        when:
        tasklet.execute(stepContribution, TaskletTestSupport.chunkContext(["PATH_ID": pathId]))

        then:
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [new Document("one.pdf", 11L, 101L, 0)]
        1 * databaseTopicStorage.getByDocumentId(11L) >> topics
        2 * openAIChapterClient.getResult(_ as ChapterGenerationRequest) >> { ChapterGenerationRequest request ->
            capturedRequests << request
            int cut = capturedRequests.size() == 1 ? 100 : 5
            new ClientResult<>(new ChapterBoundary("Part ${capturedRequests.size()}", "Partial chapter description text", cut), [])
        }
        2 * databaseLearningStorage.create(pathId, _ as Chapter, spaceId) >> { Long p, Chapter chapter, Long s ->
            persisted << chapter
        }
        capturedRequests*.topics()*.size() == [100, 5]
        persisted*.topics*.size() == [100, 5]
        persisted[1].topics == (101L..105L).toList()
    }

    def "should keep global chapter indexes across documents and preserve lesson order"() {
        given:
        def firstDocumentTopics = (1..12).collect { int i -> topic((long) i, i, ["first$i"]) }
        def secondDocumentTopics = (13..18).collect { int i -> topic((long) i, i - 12, ["second$i"]) }

        when:
        tasklet.execute(stepContribution, TaskletTestSupport.chunkContext(["PATH_ID": pathId]))

        then:
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [
                new Document("first.pdf", 11L, 101L, 0),
                new Document("second.pdf", 22L, 102L, 1)
        ]
        1 * databaseTopicStorage.getByDocumentId(11L) >> firstDocumentTopics
        1 * databaseTopicStorage.getByDocumentId(22L) >> secondDocumentTopics
        2 * openAIChapterClient.getResult(_ as ChapterGenerationRequest) >> { ChapterGenerationRequest request ->
            capturedRequests << request
            int cut = capturedRequests.size() == 1 ? 12 : 6
            new ClientResult<>(new ChapterBoundary("Doc Chapter", "Document chapter focus summary", cut), [])
        }
        2 * databaseLearningStorage.create(pathId, _ as Chapter, spaceId) >> { Long p, Chapter chapter, Long s ->
            persisted << chapter
        }
        persisted*.index == [0, 1]
        persisted*.documentId == [11L, 22L]
        persisted[0].topics == (1L..12L).toList()
        persisted[0].lessons[0].topics == (1L..3L).toList()
        persisted[0].lessons[1].topics == (4L..6L).toList()
        persisted[0].lessons[2].topics == (7L..9L).toList()
        persisted[0].lessons[3].topics == (10L..12L).toList()
        persisted[0].lessons[4].type == LessonType.CHECKPOINT
        persisted[1].topics == (13L..18L).toList()
        persisted[1].lessons[0].topics == (13L..15L).toList()
        persisted[1].lessons[1].topics == (16L..18L).toList()
        persisted[1].lessons[2].type == LessonType.CHECKPOINT
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

    private static Topic topic(Long id, int pageNumber, List<String> labels) {
        new Topic(id, null, null, pageNumber, "topic-$id", "summary-$id", labels, [ExerciseType.QUESTION])
    }
}
