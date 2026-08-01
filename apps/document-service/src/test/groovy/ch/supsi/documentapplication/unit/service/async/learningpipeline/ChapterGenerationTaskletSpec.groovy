package ch.supsi.documentapplication.unit.service.async.learningpipeline

import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.dataaccess.IDatabaseAiUsageStorage
import ch.supsi.documentapplication.dataaccess.IDatabasePathStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage
import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.model.ClientResult
import ch.supsi.documentapplication.model.learn.Chapter
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
    IDatabasePathStorage databaseLearningStorage = Mock()
    OpenAIChapterClient openAIChapterClient = Mock()
    IDatabaseAiUsageStorage aiUsageStorage = Mock()
    AppProperties appProperties = new AppProperties()
    ChapterConsolidator chapterConsolidator = new ChapterConsolidator()
    StepContribution stepContribution = Mock()

    @Subject
    ChapterGenerationTasklet tasklet = new ChapterGenerationTasklet(
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

    def "should consolidate undersized chapters before building lessons and persisting"() {
        given:
        def topics = [
                topic(1L, ["alpha"]),
                topic(2L, ["alpha"]),
                topic(3L, ["beta"]),
                topic(4L, ["gamma"]),
                topic(5L, ["gamma"]),
                topic(6L, ["gamma"])
        ]
        def aiChapters = [
                new Chapter(0, "Alpha", "Alpha chapter focus here", [0L]),
                new Chapter(1, "Beta", "Beta chapter focus here", [1L]),
                new Chapter(2, "Gamma", "Gamma chapter focus here", [2L])
        ]
        def chunkContext = TaskletTestSupport.chunkContext(["PATH_ID": pathId])
        ChapterGenerationRequest capturedRequest = null

        when:
        RepeatStatus status = tasklet.execute(stepContribution, chunkContext)

        then:
        1 * databaseTopicStorage.getBySpaceId(spaceId) >> topics
        1 * openAIChapterClient.getResult(_ as ChapterGenerationRequest) >> { ChapterGenerationRequest request ->
            capturedRequest = request
            new ClientResult<>(aiChapters, [new AiUsage(5, 7, "provider", "model")])
        }
        1 * aiUsageStorage.create("learning_path", pathId, "chapter_generate", _)
        2 * databaseLearningStorage.create(pathId, _ as Chapter, spaceId) >> { Long p, Chapter chapter, Long s ->
            persisted << chapter
        }
        status == RepeatStatus.FINISHED
        capturedRequest.maxChapters == 2
        capturedRequest.labels*.name == ["alpha", "beta", "gamma"]
        persisted.size() == 2
        persisted*.index == [0, 1]
        persisted.every { it.topics.size() >= 3 }
        persisted.every { it.lessons != null && !it.lessons.isEmpty() }
        persisted[0].topics == [1L, 2L, 3L]
        persisted[1].topics == [4L, 5L, 6L]
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
        1 * databaseTopicStorage.getBySpaceId(spaceId) >> topics
        1 * openAIChapterClient.getResult({ ChapterGenerationRequest request ->
            request.maxChapters == 1
        }) >> new ClientResult<>(aiChapters, [])
        1 * databaseLearningStorage.create(pathId, _ as Chapter, spaceId) >> { Long p, Chapter chapter, Long s ->
            persisted << chapter
        }
        status == RepeatStatus.FINISHED
        persisted.size() == 1
        persisted[0].index == 0
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
        1 * databaseTopicStorage.getBySpaceId(spaceId) >> topics
        1 * openAIChapterClient.getResult(_ as ChapterGenerationRequest) >> new ClientResult<>(aiChapters, usages)
        1 * aiUsageStorage.create("learning_path", pathId, "chapter_generate", usages[0])
        1 * aiUsageStorage.create("learning_path", pathId, "chapter_feedback", usages[1])
        1 * databaseLearningStorage.create(pathId, _ as Chapter, spaceId)
    }

    private static Topic topic(Long id, List<String> labels) {
        new Topic(id, null, null, 1, "topic-$id", "summary-$id", labels)
    }
}
