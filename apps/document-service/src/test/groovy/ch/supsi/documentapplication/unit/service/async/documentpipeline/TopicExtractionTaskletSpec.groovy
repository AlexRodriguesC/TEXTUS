package ch.supsi.documentapplication.unit.service.async.documentpipeline

import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.dataaccess.IDatabaseAiUsageStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage
import ch.supsi.documentapplication.model.ClientResult
import ch.supsi.documentapplication.model.Page
import ch.supsi.documentapplication.model.TaskType
import ch.supsi.documentapplication.model.learn.ExerciseType
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.async.documentpipeline.SetupTasklet
import ch.supsi.documentapplication.service.async.documentpipeline.TopicExtractionTasklet
import ch.supsi.documentapplication.service.clients.OpenAITopicExtractionClient
import ch.supsi.documentapplication.service.document.DocumentTaskProgressService
import ch.supsi.documentapplication.unit.service.async.TaskletTestSupport
import org.apache.commons.lang3.tuple.Pair
import org.springframework.ai.document.Document
import org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingProperties
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class TopicExtractionTaskletSpec extends Specification {

    IDatabaseDocumentStorage databaseDocumentStorage = Mock()
    IDatabaseTopicStorage databaseTopicStorage = Mock()
    OpenAITopicExtractionClient topicExtractionClient = Mock()
    IDatabaseAiUsageStorage aiUsageStorage = Mock()
    DocumentTaskProgressService documentTaskProgressService = Mock()
    AppProperties appProperties = new AppProperties()
    VectorStore vectorStore = Mock()
    StepContribution stepContribution = Mock()

    String embeddingModel = "abc"

    OpenAiEmbeddingProperties openAiEmbeddingProperties = Mock({
        getModel() >> embeddingModel
    })

    TopicExtractionTasklet topicExtractionTasklet = new TopicExtractionTasklet(
            databaseDocumentStorage,
            databaseTopicStorage,
            topicExtractionClient,
            aiUsageStorage,
            documentTaskProgressService,
            appProperties,
            vectorStore,
            openAiEmbeddingProperties)

    Long spaceId = 1L
    Long databaseDocumentId = 100L

    def setup() {
        appProperties.maxChunksPerStep = 2
        appProperties.maxParallelism = 2
        ReflectionTestUtils.setField(topicExtractionTasklet, "spaceId", spaceId)
    }

    def "should return FINISHED when no pages exist in document"() {
        given:
        def chunkContext = chunkContextWithDocumentId()

        when:
        RepeatStatus status = topicExtractionTasklet.execute(stepContribution, chunkContext)

        then:
        0 * databaseDocumentStorage.getPagesBySpaceId(_)
        0 * databaseTopicStorage.getBySpaceId(_)
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> []
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.TOPIC, 1)
        2 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC)
        0 * topicExtractionClient._
        0 * vectorStore._
        status == RepeatStatus.FINISHED
    }

    def "should return FINISHED when all document pages already have topics"() {
        given:
        def chunkContext = chunkContextWithDocumentId()
        def pages = [page(1L, 1), page(2L, 2)]
        def existingTopic = topic(10L, 1L, 1, "summary")

        when:
        RepeatStatus status = topicExtractionTasklet.execute(stepContribution, chunkContext)

        then:
        0 * databaseDocumentStorage.getPagesBySpaceId(_)
        0 * databaseTopicStorage.getBySpaceId(_)
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> pages
        _ * databaseTopicStorage.getByPageId(_) >> [existingTopic]
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.TOPIC, 1)
        2 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC)
        0 * topicExtractionClient._
        0 * vectorStore._
        status == RepeatStatus.FINISHED
    }

    def "should return CONTINUABLE when pending pages exceed maxChunksPerStep"() {
        given:
        def chunkContext = chunkContextWithDocumentId()
        def pages = [page(1L, 1), page(2L, 2), page(3L, 3)]
        def processedPageIds = [] as Set<Long>
        def extractedTopic = topic(null, 999L, 99, "summary")
        def topicIdSeq = 1000L

        when:
        RepeatStatus status = topicExtractionTasklet.execute(stepContribution, chunkContext)

        then:
        0 * databaseDocumentStorage.getPagesBySpaceId(_)
        0 * databaseTopicStorage.getBySpaceId(_)
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> pages
        _ * databaseTopicStorage.getByPageId(_) >> { Long pageId ->
            processedPageIds.contains(pageId) ? [extractedTopic] : []
        }
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.TOPIC, 3)
        0 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC)
        2 * topicExtractionClient.getResult(_ as Pair) >> { Pair<Page, List<Topic>> input ->
            processedPageIds.add(input.left.id)
            new ClientResult<>([extractedTopic], [])
        }
        2 * databaseTopicStorage.create({ Topic t ->
            processedPageIds.contains(t.page) && t.pageNumber == pages.find { it.id == t.page }.pageNumber
        }, _) >> { Topic t, Long pageId ->
            assert t.page == pageId
            topicIdSeq++
        }
        2 * vectorStore.add({ args ->
            def docs = args as List<Document>
            docs.size() == 1 &&
                    docs[0].getText() == "summary" &&
                    docs[0].getMetadata().get("embeddingModel") == embeddingModel &&
                    docs[0].getMetadata().containsKey("topicId") &&
                    docs[0].getMetadata().containsKey("pageId") &&
                    docs[0].getMetadata().get("spaceId") == spaceId
        })
        status == RepeatStatus.CONTINUABLE
    }

    def "should skip pages that already have topics on resume"() {
        given:
        def chunkContext = chunkContextWithDocumentId()
        def pages = [page(1L, 1), page(2L, 2), page(3L, 3), page(4L, 4)]
        def processedPageIds = [2L] as Set<Long>
        def existingTopic = topic(99L, 2L, 2, "existing")
        def extractedTopic = topic(null, 999L, 99, "summary")
        def topicIdSeq = 2000L

        when:
        RepeatStatus status = topicExtractionTasklet.execute(stepContribution, chunkContext)

        then:
        0 * databaseDocumentStorage.getPagesBySpaceId(_)
        0 * databaseTopicStorage.getBySpaceId(_)
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> pages
        _ * databaseTopicStorage.getByPageId(_) >> { Long pageId ->
            processedPageIds.contains(pageId) ? [existingTopic] : []
        }
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.TOPIC, 3)
        0 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC)
        2 * topicExtractionClient.getResult(_ as Pair) >> { Pair<Page, List<Topic>> input ->
            processedPageIds.add(input.left.id)
            new ClientResult<>([extractedTopic], [])
        }
        2 * databaseTopicStorage.create({ Topic t ->
            t.page == t.page && processedPageIds.contains(t.page)
        }, _) >> { Topic t, Long pageId ->
            assert t.page == pageId
            assert t.pageNumber == pages.find { it.id == pageId }.pageNumber
            topicIdSeq++
        }
        2 * vectorStore.add(_ as List)
        status == RepeatStatus.CONTINUABLE
    }

    def "should overwrite hallucinated page fields before persistence"() {
        given:
        appProperties.maxChunksPerStep = 1
        appProperties.maxParallelism = 1
        def chunkContext = chunkContextWithDocumentId()
        def pages = [page(11L, 3)]
        def hallucinated = topic(null, 847L, 999, "useful summary")
        def created = false

        when:
        RepeatStatus status = topicExtractionTasklet.execute(stepContribution, chunkContext)

        then:
        0 * databaseDocumentStorage.getPagesBySpaceId(_)
        0 * databaseTopicStorage.getBySpaceId(_)
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> pages
        _ * databaseTopicStorage.getByPageId(11L) >> { created ? [hallucinated] : [] }
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.TOPIC, 1)
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC)
        1 * topicExtractionClient.getResult(_ as Pair) >> new ClientResult<>([hallucinated], [])
        1 * databaseTopicStorage.create({ Topic t ->
            t.page == 11L && t.pageNumber == 3
        }, 11L) >> {
            created = true
            42L
        }
        1 * vectorStore.add({ args ->
            def docs = args as List<Document>
            docs.size() == 1 &&
                    docs[0].getMetadata().get("topicId") == 42L &&
                    docs[0].getMetadata().get("pageId") == 11L
        })
        status == RepeatStatus.FINISHED
    }

    def "should batch vectorStore.add once per page and skip blank summaries"() {
        given:
        appProperties.maxChunksPerStep = 1
        appProperties.maxParallelism = 1
        def chunkContext = chunkContextWithDocumentId()
        def pages = [page(1L, 1)]
        def processedPageIds = [] as Set<Long>
        def withSummary = topic(null, null, null, "useful summary")
        def blankSummary = topic(null, null, null, "  ")
        def nullSummary = new Topic(null, null, null, null, "empty", null, ["label"], [ExerciseType.QUESTION])

        when:
        RepeatStatus status = topicExtractionTasklet.execute(stepContribution, chunkContext)

        then:
        0 * databaseDocumentStorage.getPagesBySpaceId(_)
        0 * databaseTopicStorage.getBySpaceId(_)
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> pages
        _ * databaseTopicStorage.getByPageId(_) >> { Long pageId ->
            processedPageIds.contains(pageId) ? [withSummary] : []
        }
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.TOPIC, 1)
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC)
        1 * topicExtractionClient.getResult(_ as Pair) >> { Pair<Page, List<Topic>> input ->
            processedPageIds.add(input.left.id)
            new ClientResult<>([withSummary, blankSummary, nullSummary], [])
        }
        1 * databaseTopicStorage.create({ Topic t -> t.is(withSummary) && t.page == 1L && t.pageNumber == 1 }, 1L) >> 11L
        1 * databaseTopicStorage.create({ Topic t -> t.is(blankSummary) && t.page == 1L && t.pageNumber == 1 }, 1L) >> 12L
        1 * databaseTopicStorage.create({ Topic t -> t.is(nullSummary) && t.page == 1L && t.pageNumber == 1 }, 1L) >> 13L
        1 * vectorStore.add({ args ->
            def docs = args as List<Document>
            docs.size() == 1 &&
                    docs[0].getText() == "useful summary" &&
                    docs[0].getMetadata().get("topicId") == 11L &&
                    docs[0].getMetadata().get("pageId") == 1L &&
                    docs[0].getMetadata().get("spaceId") == spaceId &&
                    docs[0].getMetadata().get("embeddingModel") == embeddingModel
        })
        status == RepeatStatus.FINISHED
    }

    private chunkContextWithDocumentId() {
        TaskletTestSupport.chunkContext([(SetupTasklet.DATABASE_DOCUMENT_ID): databaseDocumentId])
    }

    private static Page page(Long id, Integer pageNumber) {
        new Page(id, pageNumber, "title-$id", "summary-$id")
    }

    private static Topic topic(Long id, Long pageId, Integer pageNumber, String summary) {
        new Topic(id, null, pageId, pageNumber, "topic", summary, ["label"], [ExerciseType.QUESTION])
    }
}
