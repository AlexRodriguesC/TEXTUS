package ch.supsi.documentapplication.unit.service.processingpipeline

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage
import ch.supsi.documentapplication.model.DocumentStatus
import ch.supsi.documentapplication.service.documentpipeline.CleanupTasklet
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class CleanupTaskletSpec extends Specification {

    IFileSystemDocumentStorage documentStorage = Mock()
    IDatabaseDocumentStorage databaseDocumentStorage = Mock()
    StepContribution stepContribution = Mock()
    ChunkContext chunkContext = Mock()

    CleanupTasklet cleanupTasklet = new CleanupTasklet(documentStorage, databaseDocumentStorage)

    def "should delete documents and finish"() {
        given:
        def expectedDocumentId = UUID.randomUUID()
        def expectedDbDocumentId = 3L
        ReflectionTestUtils.setField(cleanupTasklet, "documentId", expectedDocumentId.toString())
        ReflectionTestUtils.setField(cleanupTasklet, "databaseDocumentId", expectedDbDocumentId)
        RepeatStatus expectedStatus = RepeatStatus.FINISHED

        when:
        RepeatStatus actualStatus = cleanupTasklet.execute(stepContribution, chunkContext)

        then:
        1 * documentStorage.delete(expectedDocumentId)
        1 * databaseDocumentStorage.updateDocumentStatus(expectedDbDocumentId, DocumentStatus.DONE)
        expectedStatus == actualStatus
    }
}
