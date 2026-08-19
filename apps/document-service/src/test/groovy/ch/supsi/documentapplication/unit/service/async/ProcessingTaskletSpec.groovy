package ch.supsi.documentapplication.unit.service.async

import ch.supsi.documentapplication.service.async.ProcessingTasklet
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import spock.lang.Specification

class ProcessingTaskletSpec extends Specification {

    ProcessingTasklet tasklet = new ProcessingTasklet() {
        @Override
        RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            return RepeatStatus.FINISHED
        }
    }

    def "should return stored value when execution context contains matching type"() {
        given:
        def executionContext = new ExecutionContext()
        executionContext.put("key", "stored")

        expect:
        tasklet.getValue(executionContext, "key", "default", String) == "stored"
    }

    def "should return default value when key is missing from execution context"() {
        given:
        def executionContext = new ExecutionContext()

        expect:
        tasklet.getValue(executionContext, "missing", "default", String) == "default"
    }

    def "should return default value when stored value has wrong type"() {
        given:
        def executionContext = new ExecutionContext()
        executionContext.put("key", "stored")

        expect:
        tasklet.getValue(executionContext, "key", 0, Integer) == 0
    }
}
