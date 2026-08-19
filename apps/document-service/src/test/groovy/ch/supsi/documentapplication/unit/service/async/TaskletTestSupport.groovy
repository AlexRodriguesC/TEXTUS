package ch.supsi.documentapplication.unit.service.async

import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.scope.context.StepContext
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.infrastructure.item.ExecutionContext

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class TaskletTestSupport {

    static ChunkContext chunkContext(
            Map<String, Object> jobExecutionContextMap = [:],
            ExecutionContext stepExecutionContext = new ExecutionContext(),
            ExecutionContext jobExecutionStore = new ExecutionContext()) {
        jobExecutionContextMap.each { key, value -> jobExecutionStore.put(key, value) }

        JobExecution jobExecution = mock(JobExecution)
        when(jobExecution.getExecutionContext()).thenReturn(jobExecutionStore)

        StepExecution stepExecution = mock(StepExecution)
        when(stepExecution.getExecutionContext()).thenReturn(stepExecutionContext)
        when(stepExecution.getJobExecution()).thenReturn(jobExecution)

        StepContext stepContext = mock(StepContext)
        when(stepContext.getStepExecution()).thenReturn(stepExecution)
        when(stepContext.getJobExecutionContext()).thenReturn(new HashMap<>(jobExecutionContextMap))

        ChunkContext chunkContext = mock(ChunkContext)
        when(chunkContext.getStepContext()).thenReturn(stepContext)

        return chunkContext
    }
}
