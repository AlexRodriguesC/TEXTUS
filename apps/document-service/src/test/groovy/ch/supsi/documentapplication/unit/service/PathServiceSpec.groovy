package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.dataaccess.IDatabasePathStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage
import ch.supsi.documentapplication.exception.JobFailedToStartException
import ch.supsi.documentapplication.exception.NotFoundException
import ch.supsi.documentapplication.model.PathSettings
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.model.learn.LearningPath
import ch.supsi.documentapplication.model.learn.LearningPathStatus
import ch.supsi.documentapplication.service.PathService
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.job.JobInstance
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import spock.lang.Specification

class PathServiceSpec extends Specification {

    def pathStorage = Mock(IDatabasePathStorage)
    def spaceStorage = Mock(IDatabaseSpaceStorage)
    def jobOperator = Mock(JobOperator)
    def jobRepository = Mock(JobRepository)
    def chapterJob = Mock(Job)
    def service = new PathService(pathStorage, spaceStorage, jobOperator, jobRepository, chapterJob)

    def "should start chapter job when creating learning path"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))

        when:
        service.createLearningPath("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * jobOperator.start(chapterJob, _ as JobParameters)
    }

    def "should throw when chapter job fails to start"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))

        when:
        service.createLearningPath("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * jobOperator.start(chapterJob, _ as JobParameters) >> { throw new RuntimeException("boom") }
        thrown(JobFailedToStartException)
    }

    def "should return not created when learning path is missing"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))

        when:
        def status = service.getLearningPathStatus("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * pathStorage.read(10L) >> null
        status == LearningPathStatus.NOT_CREATED
    }

    def "should return generating when job is running"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))
        def path = new LearningPath(99L, new PathSettings(25, 25, 25, 25), [])
        def jobInstance = Mock(JobInstance)
        def jobExecution = Mock(JobExecution)

        when:
        def status = service.getLearningPathStatus("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * pathStorage.read(10L) >> path
        1 * jobRepository.getJobInstance(99L) >> jobInstance
        1 * jobRepository.getLastJobExecution(jobInstance) >> jobExecution
        1 * jobExecution.getStatus() >> BatchStatus.STARTED
        status == LearningPathStatus.GENERATING
    }

    def "should throw not found when updating settings for missing path"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))
        def settings = new PathSettings(25, 25, 25, 25)

        when:
        service.updateSettings("alice", 10L, settings)

        then:
        2 * spaceStorage.read(10L) >> space
        1 * pathStorage.read(10L) >> null
        thrown(NotFoundException)
    }

    def "should delete learning path and stop running job"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))
        def path = new LearningPath(99L, new PathSettings(25, 25, 25, 25), [])
        def jobInstance = Mock(JobInstance)
        def jobExecution = Mock(JobExecution)

        when:
        service.deleteLearningPath("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * pathStorage.read(10L) >> path
        1 * jobRepository.getJobInstance(99L) >> jobInstance
        1 * jobRepository.getLastJobExecution(jobInstance) >> jobExecution
        1 * jobExecution.isRunning() >> true
        1 * jobOperator.stop(jobExecution)
        1 * pathStorage.delete(10L)
    }
}
