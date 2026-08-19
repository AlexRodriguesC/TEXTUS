package ch.supsi.documentapplication.unit.service.async.documentpipeline

import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.dataaccess.IDatabaseAiUsageStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage
import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.model.ClientResult
import ch.supsi.documentapplication.model.Page
import ch.supsi.documentapplication.model.PageChunk
import ch.supsi.documentapplication.model.PageSummarization
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
    AiUsage usage = new AiUsage(5, 10, "test-provider", "test-model")

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
        def existingPage = new Page(1L, 1, "title", "summary")
        def pendingChunk = new PageChunk(2, "chunk content")

        when:
        summarizationTasklet.execute(stepContribution, chunkContext)

        then:
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> [existingPage]
        1 * fileSystemDocumentStorage.listChunks(documentId, Integer.MAX_VALUE) >> [pendingChunk]
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.SUMMARIZATION, 2)
        _ * fileSystemDocumentStorage.readNextChunk(documentId) >> Optional.empty()
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.SUMMARIZATION)
    }

    def "should persist accepted content page, charge usage to page, and delete chunk"() {
        given:
        def chunkContext = TaskletTestSupport.chunkContext(
                [(SetupTasklet.DATABASE_DOCUMENT_ID): databaseDocumentId])
        def chunk = new PageChunk(1, "substantive page text")

        when:
        RepeatStatus status = summarizationTasklet.execute(stepContribution, chunkContext)

        then:
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> []
        1 * fileSystemDocumentStorage.listChunks(documentId, Integer.MAX_VALUE) >> [chunk]
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.SUMMARIZATION, 1)
        _ * fileSystemDocumentStorage.readNextChunk(documentId) >>> [Optional.of(chunk), Optional.of(chunk), Optional.empty()]
        1 * fileSystemDocumentStorage.listChunks(documentId, 5) >> [chunk]
        1 * summarisationClient.getResult("substantive page text") >> new ClientResult<>(
                new PageSummarization(true, "Page title", "Page summary"), [usage])
        1 * databaseDocumentStorage.create({ it.title == "Page title" && it.summary == "Page summary" && it.pageNumber == 1 }, 1L, databaseDocumentId) >> 99L
        1 * aiUsageStorage.create("page", 99L, "summarize", usage)
        0 * aiUsageStorage.create("document", _, "summarize", _)
        1 * fileSystemDocumentStorage.deleteChunk(documentId, "1")
        1 * documentTaskProgressService.updateProgress(databaseDocumentId, TaskType.SUMMARIZATION, 1)
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.SUMMARIZATION)
        status == RepeatStatus.FINISHED
    }

    def "should discard rejected non-content page, charge usage to document, and delete chunk"() {
        given:
        def chunkContext = TaskletTestSupport.chunkContext(
                [(SetupTasklet.DATABASE_DOCUMENT_ID): databaseDocumentId])
        def chunk = new PageChunk(3, "Table of Contents")

        when:
        RepeatStatus status = summarizationTasklet.execute(stepContribution, chunkContext)

        then:
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> []
        1 * fileSystemDocumentStorage.listChunks(documentId, Integer.MAX_VALUE) >> [chunk]
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.SUMMARIZATION, 1)
        _ * fileSystemDocumentStorage.readNextChunk(documentId) >>> [Optional.of(chunk), Optional.of(chunk), Optional.empty()]
        1 * fileSystemDocumentStorage.listChunks(documentId, 5) >> [chunk]
        1 * summarisationClient.getResult("Table of Contents") >> new ClientResult<>(
                new PageSummarization(false, "", ""), [usage])
        0 * databaseDocumentStorage.create(_, _, _)
        1 * aiUsageStorage.create("document", databaseDocumentId, "summarize", usage)
        0 * aiUsageStorage.create("page", _, "summarize", _)
        1 * fileSystemDocumentStorage.deleteChunk(documentId, "3")
        1 * documentTaskProgressService.updateProgress(databaseDocumentId, TaskType.SUMMARIZATION, 1)
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.SUMMARIZATION)
        status == RepeatStatus.FINISHED
    }

    def "should skip blank chunk without calling summarization client and delete chunk"() {
        given:
        def chunkContext = TaskletTestSupport.chunkContext(
                [(SetupTasklet.DATABASE_DOCUMENT_ID): databaseDocumentId])
        def chunk = new PageChunk(4, "   ")

        when:
        RepeatStatus status = summarizationTasklet.execute(stepContribution, chunkContext)

        then:
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> []
        1 * fileSystemDocumentStorage.listChunks(documentId, Integer.MAX_VALUE) >> [chunk]
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.SUMMARIZATION, 1)
        _ * fileSystemDocumentStorage.readNextChunk(documentId) >>> [Optional.of(chunk), Optional.of(chunk), Optional.empty()]
        1 * fileSystemDocumentStorage.listChunks(documentId, 5) >> [chunk]
        0 * summarisationClient.getResult(_)
        0 * databaseDocumentStorage.create(_, _, _)
        0 * aiUsageStorage.create(_, _, _, _)
        1 * fileSystemDocumentStorage.deleteChunk(documentId, "4")
        1 * documentTaskProgressService.updateProgress(databaseDocumentId, TaskType.SUMMARIZATION, 1)
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.SUMMARIZATION)
        status == RepeatStatus.FINISHED
    }

    def "should process mixed batch and advance progress for every examined chunk"() {
        given:
        def chunkContext = TaskletTestSupport.chunkContext(
                [(SetupTasklet.DATABASE_DOCUMENT_ID): databaseDocumentId])
        def contentChunk = new PageChunk(1, "lesson content")
        def rejectedChunk = new PageChunk(2, "Index")
        def blankChunk = new PageChunk(3, "")

        when:
        RepeatStatus status = summarizationTasklet.execute(stepContribution, chunkContext)

        then:
        _ * databaseDocumentStorage.getPages(databaseDocumentId) >> []
        1 * fileSystemDocumentStorage.listChunks(documentId, Integer.MAX_VALUE) >> [contentChunk, rejectedChunk, blankChunk]
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.SUMMARIZATION, 3)
        _ * fileSystemDocumentStorage.readNextChunk(documentId) >>> [
                Optional.of(contentChunk),
                Optional.of(contentChunk),
                Optional.empty()]
        1 * fileSystemDocumentStorage.listChunks(documentId, 5) >> [contentChunk, rejectedChunk, blankChunk]
        1 * summarisationClient.getResult("lesson content") >> new ClientResult<>(
                new PageSummarization(true, "Lesson", "Summary"), [usage])
        1 * summarisationClient.getResult("Index") >> new ClientResult<>(
                new PageSummarization(false, "", ""), [usage])
        1 * databaseDocumentStorage.create({ it.title == "Lesson" && it.pageNumber == 1 }, 1L, databaseDocumentId) >> 50L
        1 * aiUsageStorage.create("page", 50L, "summarize", usage)
        1 * aiUsageStorage.create("document", databaseDocumentId, "summarize", usage)
        1 * fileSystemDocumentStorage.deleteChunk(documentId, "1")
        1 * fileSystemDocumentStorage.deleteChunk(documentId, "2")
        1 * fileSystemDocumentStorage.deleteChunk(documentId, "3")
        1 * documentTaskProgressService.updateProgress(databaseDocumentId, TaskType.SUMMARIZATION, 3)
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.SUMMARIZATION)
        status == RepeatStatus.FINISHED
    }
}
