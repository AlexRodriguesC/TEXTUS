package ch.supsi.documentapplication.unit.service.batch

import ch.supsi.documentapplication.service.batch.JobExecutionResolver
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.job.JobInstance
import org.springframework.batch.core.repository.JobRepository
import spock.lang.Specification

class JobExecutionResolverSpec extends Specification {

    def jobRepository = Mock(JobRepository)

    def "should return null when job instance does not exist"() {
        when:
        def result = JobExecutionResolver.getLatestJobExecution(jobRepository, 1L)

        then:
        1 * jobRepository.getJobInstance(1L) >> null
        result == null
    }

    def "should return last job execution when job instance exists"() {
        given:
        def jobInstance = Mock(JobInstance)
        def jobExecution = Mock(JobExecution)

        when:
        def result = JobExecutionResolver.getLatestJobExecution(jobRepository, 1L)

        then:
        1 * jobRepository.getJobInstance(1L) >> jobInstance
        1 * jobRepository.getLastJobExecution(jobInstance) >> jobExecution
        result == jobExecution
    }
}
