package ch.supsi.documentapplication.unit.service.async.documentpipeline

import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.dataaccess.IDatabaseAiUsageStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage
import ch.supsi.documentapplication.model.TaskType
import ch.supsi.documentapplication.service.async.documentpipeline.SetupTasklet
import ch.supsi.documentapplication.service.async.documentpipeline.SummarizationTasklet
import ch.supsi.documentapplication.service.clients.OpenAISummarizationClient
import ch.supsi.documentapplication.service.document.DocumentTaskProgressService
import ch.supsi.documentapplication.unit.service.async.TaskletTestSupport
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class SummarizationTaskletSpec extends Specification {

    IDatabaseDocumentStorage databaseDocumentStorage = Mock()
    IFileSystemDocumentStorage fileSystemDocumentStorage = Mock()
    OpenAISummarizationClient summarisationClient = Mock()
    IDatabaseAiUsageStorage aiUsageStorage = Mock()
    DocumentTaskProgressService documentTaskProgressService = Mock()
    AppProperties appProperties = new AppProperties()
    StepContribution stepContribution = Mock()

    SummarizationTasklet summarizationTasklet = new SummarizationTasklet(
            databaseDocumentStorage,
            fileSystemDocumentStorage,
            summarisationClient,
            aiUsageStorage,
            documentTaskProgressService,
            appProperties)

    UUID documentId = UUID.randomUUID()
    Long databaseDocumentId = 10L

    def setup() {
        appProperties.maxChunksPerStep = 5
        appProperties.maxParallelism = 2
        ReflectionTestUtils.setField(summarizationTasklet, "documentId", documentId.toString())
        ReflectionTestUtils.setField(summarizationTasklet, "spaceId", 1L)
    }

    def "should complete summarization task when no chunks remain"() {
        given:
        def chunkContext = TaskletTestSupport.chunkContext(
                [(SetupTasklet.DATABASE_DOCUMENT_ID): databaseDocumentId])

        when:
        RepeatStatus status = summarizationTasklet.execute(stepContribution, chunkContext)

        then:
        1 * databaseDocumentStorage.getPages(databaseDocumentId) >> []
        1 * fileSystemDocumentStorage.listChunks(documentId, Integer.MAX_VALUE) >> []
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.SUMMARIZATION, 1)
        1 * fileSystemDocumentStorage.readNextChunk(documentId) >> Optional.empty()
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.SUMMARIZATION)
        status == RepeatStatus.FINISHED
    }

    def "should initialize progress from persisted pages and remaining chunks"() {
        given:
        def chunkContext = TaskletTestSupport.chunkContext(
                [(SetupTasklet.DATABASE_DOCUMENT_ID): databaseDocumentId])
        def existingPage = new ch.supsi.documentapplication.model.Page(1L, "title", "summary")
        def pendingChunk = new ch.supsi.documentapplication.model.PageChunk(2, "chunk content")

        when:
        summarizationTasklet.execute(stepContribution, chunkContext)

        then:
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> [existingPage]
        1 * fileSystemDocumentStorage.listChunks(documentId, Integer.MAX_VALUE) >> [pendingChunk]
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.SUMMARIZATION, 2)
        _ * fileSystemDocumentStorage.readNextChunk(documentId) >> Optional.empty()
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.SUMMARIZATION)
    }
}
