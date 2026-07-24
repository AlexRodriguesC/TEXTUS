package ch.supsi.documentapplication.unit.service.async.documentpipeline

import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.dataaccess.IDatabaseAiUsageStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage
import ch.supsi.documentapplication.model.ClientResult
import ch.supsi.documentapplication.model.Page
import ch.supsi.documentapplication.model.SpacePage
import ch.supsi.documentapplication.model.TaskType
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

    def "should return FINISHED when no pages exist in space"() {
        given:
        def chunkContext = chunkContextWithDocumentId()

        when:
        RepeatStatus status = topicExtractionTasklet.execute(stepContribution, chunkContext)

        then:
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> []
        1 * databaseDocumentStorage.getPagesBySpaceId(spaceId) >> []
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.TOPIC, 1)
        2 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC)
        0 * topicExtractionClient._
        0 * vectorStore._
        status == RepeatStatus.FINISHED
    }

    def "should return FINISHED when all pages already have topics"() {
        given:
        def chunkContext = chunkContextWithDocumentId()
        def pages = [spacePage(1L), spacePage(2L)]
        def existingTopic = topic(10L, "summary")

        when:
        RepeatStatus status = topicExtractionTasklet.execute(stepContribution, chunkContext)

        then:
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> []
        1 * databaseDocumentStorage.getPagesBySpaceId(spaceId) >> pages
        2 * databaseTopicStorage.getByPageId(_) >> [existingTopic]
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.TOPIC, 1)
        2 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC)
        0 * topicExtractionClient._
        0 * vectorStore._
        status == RepeatStatus.FINISHED
    }

    def "should return CONTINUABLE when pending pages exceed maxChunksPerStep"() {
        given:
        def chunkContext = chunkContextWithDocumentId()
        def pages = [spacePage(1L), spacePage(2L), spacePage(3L)]
        def processedPageIds = [] as Set<Long>
        def extractedTopic = topic(null, "summary")
        def topicIdSeq = 1000L

        when:
        RepeatStatus status = topicExtractionTasklet.execute(stepContribution, chunkContext)

        then:
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> []
        2 * databaseDocumentStorage.getPagesBySpaceId(spaceId) >> pages
        _ * databaseTopicStorage.getByPageId(_) >> { Long pageId ->
            processedPageIds.contains(pageId) ? [extractedTopic] : []
        }
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.TOPIC, 1)
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC)
        1 * databaseTopicStorage.getBySpaceId(spaceId) >> []
        2 * topicExtractionClient.getResult(_ as Pair) >> { Pair<Page, List<Topic>> input ->
            processedPageIds.add(input.left.id)
            new ClientResult<>([extractedTopic], [])
        }
        2 * databaseTopicStorage.create(extractedTopic, _) >> { Topic t, Long pageId -> topicIdSeq++ }
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
        def pages = [spacePage(1L), spacePage(2L), spacePage(3L), spacePage(4L)]
        def processedPageIds = [2L] as Set<Long>
        def existingTopic = topic(99L, "existing")
        def extractedTopic = topic(null, "summary")
        def topicIdSeq = 2000L

        when:
        RepeatStatus status = topicExtractionTasklet.execute(stepContribution, chunkContext)

        then:
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> []
        2 * databaseDocumentStorage.getPagesBySpaceId(spaceId) >> pages
        _ * databaseTopicStorage.getByPageId(_) >> { Long pageId ->
            processedPageIds.contains(pageId) ? [existingTopic] : []
        }
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.TOPIC, 1)
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC)
        1 * databaseTopicStorage.getBySpaceId(spaceId) >> [existingTopic]
        2 * topicExtractionClient.getResult(_ as Pair) >> { Pair<Page, List<Topic>> input ->
            processedPageIds.add(input.left.id)
            new ClientResult<>([extractedTopic], [])
        }
        2 * databaseTopicStorage.create(extractedTopic, _) >> { Topic t, Long pageId -> topicIdSeq++ }
        2 * vectorStore.add(_ as List)
        status == RepeatStatus.CONTINUABLE
    }

    def "should batch vectorStore.add once per page and skip blank summaries"() {
        given:
        appProperties.maxChunksPerStep = 1
        appProperties.maxParallelism = 1
        def chunkContext = chunkContextWithDocumentId()
        def pages = [spacePage(1L)]
        def processedPageIds = [] as Set<Long>
        def withSummary = topic(null, "useful summary")
        def blankSummary = topic(null, "  ")
        def nullSummary = new Topic(null, null, null, "empty", null, ["label"])

        when:
        RepeatStatus status = topicExtractionTasklet.execute(stepContribution, chunkContext)

        then:
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> []
        2 * databaseDocumentStorage.getPagesBySpaceId(spaceId) >> pages
        _ * databaseTopicStorage.getByPageId(_) >> { Long pageId ->
            processedPageIds.contains(pageId) ? [withSummary] : []
        }
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.TOPIC, 1)
        2 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC)
        1 * databaseTopicStorage.getBySpaceId(spaceId) >> []
        1 * topicExtractionClient.getResult(_ as Pair) >> { Pair<Page, List<Topic>> input ->
            processedPageIds.add(input.left.id)
            new ClientResult<>([withSummary, blankSummary, nullSummary], [])
        }
        1 * databaseTopicStorage.create(withSummary, 1L) >> 11L
        1 * databaseTopicStorage.create(blankSummary, 1L) >> 12L
        1 * databaseTopicStorage.create(nullSummary, 1L) >> 13L
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

    private static SpacePage spacePage(Long id) {
        new SpacePage(id, 100L, "title-$id", "summary-$id")
    }

    private static Topic topic(Long id, String summary) {
        new Topic(id, null, null, "topic", summary, ["label"])
    }
}
