package ch.supsi.documentapplication.unit.service.async.learningpipeline

import ch.supsi.documentapplication.dataaccess.IDatabasePathStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.service.async.learningpipeline.SetupTasklet
import ch.supsi.documentapplication.unit.service.async.TaskletTestSupport
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class SetupTaskletSpec extends Specification {

    IDatabaseSpaceStorage databaseSpaceStorage = Mock()
    IDatabasePathStorage databasePathStorage = Mock()
    StepContribution stepContribution = Mock()
    SetupTasklet setupTasklet = new SetupTasklet(databaseSpaceStorage, databasePathStorage)

    def setup() {
        ReflectionTestUtils.setField(setupTasklet, "spaceId", 1L)
        ReflectionTestUtils.setField(setupTasklet, "jobInstanceId", 42L)
    }

    def "should create learning path and store path id when space exists"() {
        given:
        def jobExecutionContext = new ExecutionContext()
        def chunkContext = TaskletTestSupport.chunkContext([:], new ExecutionContext(), jobExecutionContext)
        def space = new Space(1L, "owner", "icon", "space", "desc")

        when:
        RepeatStatus status = setupTasklet.execute(stepContribution, chunkContext)

        then:
        1 * databaseSpaceStorage.read(1L) >> Optional.of(space)
        1 * databasePathStorage.createLearningPath(1L, 42L) >> 55L
        status == RepeatStatus.FINISHED
        jobExecutionContext.get("PATH_ID") == 55L
    }
}
