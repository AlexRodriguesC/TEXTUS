package ch.supsi.documentapplication.unit.service.async.documentpipeline

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage
import ch.supsi.documentapplication.service.async.documentpipeline.SetupTasklet
import ch.supsi.documentapplication.unit.service.async.TaskletTestSupport
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class SetupTaskletSpec extends Specification {

    IDatabaseDocumentStorage databaseDocumentStorage = Mock()
    StepContribution stepContribution = Mock()
    SetupTasklet setupTasklet = new SetupTasklet(databaseDocumentStorage)

    def setup() {
        ReflectionTestUtils.setField(setupTasklet, "jobInstanceId", 42L)
        ReflectionTestUtils.setField(setupTasklet, "documentName", "test.pdf")
        ReflectionTestUtils.setField(setupTasklet, "spaceId", 1L)
    }

    def "should create document record and finish when setup has not run yet"() {
        given:
        def stepExecutionContext = new ExecutionContext()
        def chunkContext = TaskletTestSupport.chunkContext([:], stepExecutionContext)

        when:
        RepeatStatus status = setupTasklet.execute(stepContribution, chunkContext)

        then:
        1 * databaseDocumentStorage.findIdByJobId(42L) >> Optional.empty()
        1 * databaseDocumentStorage.create(1L, 42L, "test.pdf") >> 99L
        status == RepeatStatus.FINISHED
        stepExecutionContext.get(SetupTasklet.DATABASE_DOCUMENT_ID) == 99L
    }

    def "should skip creation and finish when setup was already completed"() {
        given:
        def chunkContext = TaskletTestSupport.chunkContext(
                [(SetupTasklet.DATABASE_DOCUMENT_ID): 99L],
                new ExecutionContext())

        when:
        RepeatStatus status = setupTasklet.execute(stepContribution, chunkContext)

        then:
        0 * databaseDocumentStorage._
        status == RepeatStatus.FINISHED
    }
}
