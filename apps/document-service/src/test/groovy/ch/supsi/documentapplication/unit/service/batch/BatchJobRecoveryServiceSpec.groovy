package ch.supsi.documentapplication.unit.service.batch

import ch.supsi.documentapplication.service.batch.BatchJobRecoveryService
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.core.task.TaskExecutor
import spock.lang.Specification

class BatchJobRecoveryServiceSpec extends Specification {

    def jobRepository = Mock(JobRepository)
    def jobOperator = Mock(JobOperator)
    def taskExecutor = Mock(TaskExecutor)
    def service = new BatchJobRecoveryService(jobRepository, jobOperator, taskExecutor)

    def "should recover interrupted job execution"() {
        given:
        def jobExecution = Mock(JobExecution)
        def recovered = Mock(JobExecution)

        when:
        def result = service.recoverIfNeeded(jobExecution)

        then:
        1 * jobExecution.getStatus() >> BatchStatus.STARTED
        1 * jobOperator.recover(jobExecution) >> recovered
        result == recovered
    }

    def "should return same execution when not interrupted"() {
        given:
        def jobExecution = Mock(JobExecution)

        when:
        def result = service.recoverIfNeeded(jobExecution)

        then:
        1 * jobExecution.getStatus() >> BatchStatus.COMPLETED
        0 * jobOperator._
        result == jobExecution
    }

    def "should recover and restart job execution"() {
        given:
        def jobExecution = Mock(JobExecution)
        def recovered = Mock(JobExecution)
        def restarted = Mock(JobExecution)

        when:
        def result = service.recoverAndRestart(jobExecution)

        then:
        1 * jobExecution.getStatus() >> BatchStatus.STARTED
        1 * jobOperator.recover(jobExecution) >> recovered
        1 * jobOperator.restart(recovered) >> restarted
        result == restarted
    }

    def "should submit startup recovery to batch task executor"() {
        when:
        service.recoverInterruptedJobsOnStartup()

        then:
        1 * taskExecutor.execute(_)
        0 * jobRepository._
        0 * jobOperator._
    }
}
