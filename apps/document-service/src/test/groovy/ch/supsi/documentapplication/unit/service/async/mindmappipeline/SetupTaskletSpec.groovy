package ch.supsi.documentapplication.unit.service.async.mindmappipeline

import ch.supsi.documentapplication.dataaccess.DatabaseSpaceStorage
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.service.async.mindmappipeline.SetupTasklet
import ch.supsi.documentapplication.unit.service.async.TaskletTestSupport
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class SetupTaskletSpec extends Specification {

    DatabaseSpaceStorage databaseSpaceStorage = Mock()
    StepContribution stepContribution = Mock()
    SetupTasklet setupTasklet = new SetupTasklet(databaseSpaceStorage)

    def setup() {
        ReflectionTestUtils.setField(setupTasklet, "spaceId", 1L)
        ReflectionTestUtils.setField(setupTasklet, "jobInstanceId", 42L)
    }

    def "should create mind map and store map id when space exists"() {
        given:
        def jobExecutionContext = new ExecutionContext()
        def chunkContext = TaskletTestSupport.chunkContext([:], new ExecutionContext(), jobExecutionContext)
        def space = new Space(1L, "owner", "icon", "space", "desc")

        when:
        RepeatStatus status = setupTasklet.execute(stepContribution, chunkContext)

        then:
        1 * databaseSpaceStorage.read(1L) >> Optional.of(space)
        1 * databaseSpaceStorage.createMindMap(1L, 42L) >> 77L
        status == RepeatStatus.FINISHED
        jobExecutionContext.get("MAP_ID") == 77L
    }
}
