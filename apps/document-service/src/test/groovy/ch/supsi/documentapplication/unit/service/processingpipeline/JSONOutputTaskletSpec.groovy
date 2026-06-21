package ch.supsi.documentapplication.unit.service.processingpipeline

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage
import ch.supsi.documentapplication.model.Chunk
import ch.supsi.documentapplication.service.processingpipeline.JSONOutputTasklet
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.scope.context.StepContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class JSONOutputTaskletSpec extends Specification {

    IDatabaseDocumentStorage databaseDocumentStorage = Mock()
    IFileSystemDocumentStorage fileSystemDocumentStorage = Mock()
    StepContribution stepContribution = Mock()
    ChunkContext chunkContext = Mock()
    StepContext stepContext = Mock()
    StepExecution stepExecution = Mock()
    JSONOutputTasklet jsonOutputTasklet = new JSONOutputTasklet(databaseDocumentStorage, fileSystemDocumentStorage)

    def setup() {
        chunkContext.getStepContext() >> stepContext
        stepContext.getStepExecution() >> stepExecution
    }

    def "creates json output from chunk ids in job execution context"() {
        given:
        UUID expectedDocumentId = UUID.randomUUID()
        ReflectionTestUtils.setField(jsonOutputTasklet, "documentId", expectedDocumentId.toString())

        def jobExecutionContext = ["CHUNK_IDS": [1L, 2L, 3L]]
        stepContext.getJobExecutionContext() >> jobExecutionContext

        def chunk1 = Stub(Chunk)
        def chunk2 = Stub(Chunk)
        def chunk3 = Stub(Chunk)

        databaseDocumentStorage.read(1L) >> chunk1
        databaseDocumentStorage.read(2L) >> chunk2
        databaseDocumentStorage.read(3L) >> chunk3

        when:
        def actualStatus = jsonOutputTasklet.execute(stepContribution, chunkContext)

        then:
        1 * fileSystemDocumentStorage.createChunkJSON([chunk1, chunk2, chunk3], expectedDocumentId)
        actualStatus == RepeatStatus.FINISHED
    }

    def "creates json output with empty chunk list when no chunk ids are present"() {
        given:
        UUID expectedDocumentId = UUID.randomUUID()
        ReflectionTestUtils.setField(jsonOutputTasklet, "documentId", expectedDocumentId.toString())

        def jobExecutionContext = ["CHUNK_IDS": []]
        stepContext.getJobExecutionContext() >> jobExecutionContext

        when:
        def actualStatus = jsonOutputTasklet.execute(stepContribution, chunkContext)

        then:
        1 * fileSystemDocumentStorage.createChunkJSON([], expectedDocumentId)
        actualStatus == RepeatStatus.FINISHED
    }

    def "creates json output with empty chunk list when chunk ids are empty"() {
        given:
        UUID expectedDocumentId = UUID.randomUUID()
        ReflectionTestUtils.setField(jsonOutputTasklet, "documentId", expectedDocumentId.toString())

        def jobExecutionContext = ["CHUNK_IDS": []]
        stepContext.getJobExecutionContext() >> jobExecutionContext

        when:
        def actualStatus = jsonOutputTasklet.execute(stepContribution, chunkContext)

        then:
        1 * fileSystemDocumentStorage.createChunkJSON([], expectedDocumentId)
        actualStatus == RepeatStatus.FINISHED
    }

    def "does not read chunks when chunk ids list is empty"() {
        given:
        UUID expectedDocumentId = UUID.randomUUID()
        ReflectionTestUtils.setField(jsonOutputTasklet, "documentId", expectedDocumentId.toString())

        def jobExecutionContext = ["CHUNK_IDS": []]
        stepContext.getJobExecutionContext() >> jobExecutionContext

        when:
        jsonOutputTasklet.execute(stepContribution, chunkContext)

        then:
        0 * databaseDocumentStorage._
        1 * fileSystemDocumentStorage.createChunkJSON([], expectedDocumentId)
    }
}