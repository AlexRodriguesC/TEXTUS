package ch.supsi.documentapplication.unit.service.async.documentpipeline

import ch.supsi.documentapplication.model.TaskType
import ch.supsi.documentapplication.service.async.documentpipeline.DocumentTaskStepListener
import ch.supsi.documentapplication.service.async.documentpipeline.SetupTasklet
import ch.supsi.documentapplication.service.document.DocumentTaskProgressService
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.step.StepExecution
import org.springframework.batch.infrastructure.item.ExecutionContext
import spock.lang.Specification

class DocumentTaskStepListenerSpec extends Specification {

    DocumentTaskProgressService documentTaskProgressService = Mock()
    DocumentTaskStepListener listener = new DocumentTaskStepListener(documentTaskProgressService)

    def "beforeStep resumes failed task without resetting progress"() {
        given:
        def stepExecution = stepExecution("OCRTasklet", 42L)

        when:
        listener.beforeStep(stepExecution)

        then:
        1 * documentTaskProgressService.resumeTask(42L, TaskType.OCR)
        0 * documentTaskProgressService._
    }

    def "beforeStep ignores unknown steps"() {
        given:
        def stepExecution = stepExecution("CleanupTasklet", 42L)

        when:
        listener.beforeStep(stepExecution)

        then:
        0 * documentTaskProgressService._
    }

    def "afterStep marks task failed on failure"() {
        given:
        def stepExecution = stepExecution("SummarizationTasklet", 7L, ExitStatus.FAILED)

        when:
        def exitStatus = listener.afterStep(stepExecution)

        then:
        exitStatus == ExitStatus.FAILED
        1 * documentTaskProgressService.failTask(7L, TaskType.SUMMARIZATION)
        0 * documentTaskProgressService._
    }

    def "afterStep marks TOPIC failed for CGLIB-enhanced step name"() {
        given:
        def stepExecution = stepExecution("TopicExtractionTasklet\$\$SpringCGLIB\$\$0", 9L, ExitStatus.FAILED)

        when:
        def exitStatus = listener.afterStep(stepExecution)

        then:
        exitStatus == ExitStatus.FAILED
        1 * documentTaskProgressService.failTask(9L, TaskType.TOPIC)
        0 * documentTaskProgressService._
    }

    def "afterStep does not fail task on success"() {
        given:
        def stepExecution = stepExecution("TopicExtractionTasklet", 7L, ExitStatus.COMPLETED)

        when:
        listener.afterStep(stepExecution)

        then:
        0 * documentTaskProgressService._
    }

    private StepExecution stepExecution(String stepName, Long documentId, ExitStatus exitStatus = ExitStatus.COMPLETED) {
        def jobContext = new ExecutionContext()
        jobContext.put(SetupTasklet.DATABASE_DOCUMENT_ID, documentId)

        JobExecution jobExecution = Mock()
        jobExecution.getExecutionContext() >> jobContext

        StepExecution stepExecution = Mock()
        stepExecution.getStepName() >> stepName
        stepExecution.getJobExecution() >> jobExecution
        stepExecution.getExitStatus() >> exitStatus
        return stepExecution
    }
}
