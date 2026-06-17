package ch.supsi.textusbackend.unit.service.processingpipeline

import ch.supsi.textusbackend.dataaccess.IFileSystemBookStorage
import ch.supsi.textusbackend.service.processingpipeline.CleanupTasklet
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class CleanupTaskletSpec extends Specification {

    IFileSystemBookStorage bookStorage = Mock()
    StepContribution stepContribution = Mock()
    ChunkContext chunkContext = Mock()

    CleanupTasklet cleanupTasklet = new CleanupTasklet(bookStorage)

    def "should delete books and finish"() {
        given:
        def expectedBookId = 42L
        ReflectionTestUtils.setField(cleanupTasklet, "bookId", expectedBookId)
        RepeatStatus expectedStatus = RepeatStatus.FINISHED

        when:
        RepeatStatus actualStatus = cleanupTasklet.execute(stepContribution, chunkContext)

        then:
        1 * bookStorage.delete(expectedBookId)
        expectedStatus == actualStatus
    }
}
