package ch.supsi.documentapplication.unit.service.processingpipeline


import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage
import ch.supsi.documentapplication.service.async.documentpipeline.CleanupTasklet
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class CleanupTaskletSpec extends Specification {

    IFileSystemDocumentStorage documentStorage = Mock()
    StepContribution stepContribution = Mock()
    ChunkContext chunkContext = Mock()

    CleanupTasklet cleanupTasklet = new CleanupTasklet(documentStorage)

    def "should delete documents and finish"() {
        given:
        def expectedDocumentId = UUID.randomUUID()
        ReflectionTestUtils.setField(cleanupTasklet, "documentId", expectedDocumentId.toString())
        RepeatStatus expectedStatus = RepeatStatus.FINISHED

        when:
        RepeatStatus actualStatus = cleanupTasklet.execute(stepContribution, chunkContext)

        then:
        1 * documentStorage.delete(expectedDocumentId)
        expectedStatus == actualStatus
    }
}
