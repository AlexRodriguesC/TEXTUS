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
    StepContribution stepContribution = Mock()

    TopicExtractionTasklet topicExtractionTasklet = new TopicExtractionTasklet(
            databaseDocumentStorage,
            databaseTopicStorage,
            topicExtractionClient,
            aiUsageStorage,
            documentTaskProgressService,
            appProperties)

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
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC)
        0 * topicExtractionClient._
        status == RepeatStatus.FINISHED
    }

    def "should return FINISHED when all pages already have topics"() {
        given:
        def chunkContext = chunkContextWithDocumentId()
        def pages = [spacePage(1L), spacePage(2L)]
        def existingTopic = topic(10L)

        when:
        RepeatStatus status = topicExtractionTasklet.execute(stepContribution, chunkContext)

        then:
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> []
        1 * databaseDocumentStorage.getPagesBySpaceId(spaceId) >> pages
        2 * databaseTopicStorage.getByPageId(_) >> [existingTopic]
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.TOPIC, 1)
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC)
        0 * topicExtractionClient._
        status == RepeatStatus.FINISHED
    }

    def "should return CONTINUABLE when pending pages exceed maxChunksPerStep"() {
        given:
        def chunkContext = chunkContextWithDocumentId()
        def pages = [spacePage(1L), spacePage(2L), spacePage(3L)]
        def processedPageIds = [] as Set<Long>
        def extractedTopic = topic(null)

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
        2 * databaseTopicStorage.create(extractedTopic, _)
        status == RepeatStatus.CONTINUABLE
    }

    def "should skip pages that already have topics on resume"() {
        given:
        def chunkContext = chunkContextWithDocumentId()
        def pages = [spacePage(1L), spacePage(2L), spacePage(3L), spacePage(4L)]
        def processedPageIds = [2L] as Set<Long>
        def existingTopic = topic(99L)
        def extractedTopic = topic(null)

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
        2 * databaseTopicStorage.create(extractedTopic, _)
        status == RepeatStatus.CONTINUABLE
    }

    private chunkContextWithDocumentId() {
        TaskletTestSupport.chunkContext([(SetupTasklet.DATABASE_DOCUMENT_ID): databaseDocumentId])
    }

    private static SpacePage spacePage(Long id) {
        new SpacePage(id, 100L, "title-$id", "summary-$id")
    }

    private static Topic topic(Long id) {
        new Topic(id, null, null, "topic", "summary", ["label"])
    }
}
