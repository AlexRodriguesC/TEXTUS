package ch.supsi.documentapplication.unit.service.async.mindmappipeline

import ch.supsi.documentapplication.dataaccess.DatabaseSpaceStorage
import ch.supsi.documentapplication.model.MindMap
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.service.async.mindmappipeline.SetupTasklet
import ch.supsi.documentapplication.unit.service.async.TaskletTestSupport
import org.springframework.batch.core.ExitStatus
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
    }

    def "should load existing mind map id into execution context when space exists"() {
        given:
        def jobExecutionContext = new ExecutionContext()
        def chunkContext = TaskletTestSupport.chunkContext([:], new ExecutionContext(), jobExecutionContext)
        def space = new Space(1L, "owner", "icon", "space", "desc")
        def mindMap = new MindMap()
        mindMap.id = 77L

        when:
        RepeatStatus status = setupTasklet.execute(stepContribution, chunkContext)

        then:
        1 * databaseSpaceStorage.read(1L) >> Optional.of(space)
        1 * databaseSpaceStorage.readMindMap(1L) >> mindMap
        0 * databaseSpaceStorage.createMindMap(_, _)
        status == RepeatStatus.FINISHED
        jobExecutionContext.get("MAP_ID") == 77L
    }

    def "should fail when mind map row is missing"() {
        given:
        def jobExecutionContext = new ExecutionContext()
        def chunkContext = TaskletTestSupport.chunkContext([:], new ExecutionContext(), jobExecutionContext)
        def space = new Space(1L, "owner", "icon", "space", "desc")

        when:
        RepeatStatus status = setupTasklet.execute(stepContribution, chunkContext)

        then:
        1 * databaseSpaceStorage.read(1L) >> Optional.of(space)
        1 * databaseSpaceStorage.readMindMap(1L) >> null
        1 * stepContribution.setExitStatus(ExitStatus.FAILED)
        status == RepeatStatus.FINISHED
        jobExecutionContext.get("MAP_ID") == null
    }
}
