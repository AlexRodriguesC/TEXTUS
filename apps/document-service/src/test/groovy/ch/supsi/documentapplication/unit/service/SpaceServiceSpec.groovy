package ch.supsi.documentapplication.unit.service


import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage
import ch.supsi.documentapplication.exception.JobFailedToStartException
import ch.supsi.documentapplication.exception.NotFoundException
import ch.supsi.documentapplication.exception.UnauthorizedException
import ch.supsi.documentapplication.model.MindMap
import ch.supsi.documentapplication.model.MindMapStatus
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.service.SpaceService
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.job.JobInstance
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.core.task.SyncTaskExecutor
import spock.lang.Specification

class SpaceServiceSpec extends Specification {

    def spaceStorage = Mock(IDatabaseSpaceStorage)
    def jobOperator = Mock(JobOperator)
    def mindMapJob = Mock(Job)
    def jobRepository = Mock(JobRepository)
    def batchTaskExecutor = new SyncTaskExecutor()

    def service = new SpaceService(spaceStorage, jobOperator, mindMapJob, jobRepository)

    def "should return mind map for authorized user"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))
        def mindMap = Mock(MindMap)

        when:
        def result = service.getMindMap("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * spaceStorage.readMindMap(10L) >> mindMap
        result == mindMap
    }

    def "should throw not found when space does not exist for mind map"() {
        when:
        service.getMindMap("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> Optional.empty()
        def e = thrown(NotFoundException)
        e.message
        0 * spaceStorage.readMindMap(_)
    }

    def "should throw unauthorized when requester is not owner of mind map space"() {
        given:
        def space = Optional.of(new Space(10L, "bob", "", "", ""))

        when:
        service.getMindMap("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        def e = thrown(UnauthorizedException)
        e.message
        0 * spaceStorage.readMindMap(_)
    }

    def "should return not created when mind map is missing for status"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))

        when:
        def status = service.getMindMapStatus("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * spaceStorage.readMindMap(10L) >> null
        status == MindMapStatus.NOT_CREATED
    }

    def "should return generating when job is running"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))
        def mindMap = new MindMap()
        mindMap.setJobId(99L)
        def jobInstance = Mock(JobInstance)
        def jobExecution = Mock(JobExecution)

        when:
        def status = service.getMindMapStatus("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * spaceStorage.readMindMap(10L) >> mindMap
        1 * jobRepository.getJobInstance(99L) >> jobInstance
        1 * jobRepository.getLastJobExecution(jobInstance) >> jobExecution
        1 * jobExecution.getStatus() >> org.springframework.batch.core.BatchStatus.STARTED
        status == MindMapStatus.GENERATING
    }

    def "should return failed when job is unsuccessful"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))
        def mindMap = new MindMap()
        mindMap.setJobId(99L)
        def jobInstance = Mock(JobInstance)
        def jobExecution = Mock(JobExecution)

        when:
        def status = service.getMindMapStatus("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * spaceStorage.readMindMap(10L) >> mindMap
        1 * jobRepository.getJobInstance(99L) >> jobInstance
        1 * jobRepository.getLastJobExecution(jobInstance) >> jobExecution
        1 * jobExecution.getStatus() >> org.springframework.batch.core.BatchStatus.FAILED
        status == MindMapStatus.FAILED
    }

    def "should delete mind map by id and stop running job"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))
        def mindMap = new MindMap()
        mindMap.setId(7L)
        mindMap.setJobId(99L)
        def jobInstance = Mock(JobInstance)
        def jobExecution = Mock(JobExecution)

        when:
        service.deleteMindMap("alice", 10L, 7L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * spaceStorage.readMindMap(10L) >> mindMap
        1 * jobRepository.getJobInstance(99L) >> jobInstance
        1 * jobRepository.getLastJobExecution(jobInstance) >> jobExecution
        1 * jobExecution.isRunning() >> true
        1 * jobOperator.stop(jobExecution)
        1 * spaceStorage.deleteMindMap(10L)
    }

    def "should throw not found when deleting mind map with mismatched id"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))
        def mindMap = new MindMap()
        mindMap.setId(7L)
        mindMap.setJobId(99L)

        when:
        service.deleteMindMap("alice", 10L, 99L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * spaceStorage.readMindMap(10L) >> mindMap
        thrown(NotFoundException)
        0 * spaceStorage.deleteMindMap(_)
    }

    def "should create mind map row before starting job and return generating summary"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))
        def jobInstance = Mock(JobInstance)
        def jobExecution = Mock(JobExecution)
        mindMapJob.getName() >> "mind_map_creation_job"

        when:
        def summary = service.createMindMap("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * jobRepository.createJobInstance("mind_map_creation_job", _ as JobParameters) >> jobInstance
        1 * jobInstance.getInstanceId() >> 99L
        1 * spaceStorage.createMindMap(10L, 99L) >> 42L
        1 * jobRepository.createJobExecution(jobInstance, _ as JobParameters, _ as ExecutionContext) >> jobExecution
        1 * mindMapJob.execute(jobExecution)
        summary.id() == 42L
        summary.status() == MindMapStatus.GENERATING
    }

    def "should throw domain exception when mind map job fails to start"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))
        mindMapJob.getName() >> "mind_map_creation_job"

        when:
        service.createMindMap("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * jobRepository.createJobInstance("mind_map_creation_job", _ as JobParameters) >> {
            throw new RuntimeException("boom")
        }
        thrown(JobFailedToStartException)
    }

    def "should list mind maps with resolved status"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))
        def mindMap = new MindMap()
        mindMap.setId(7L)
        mindMap.setJobId(99L)
        def jobInstance = Mock(JobInstance)
        def jobExecution = Mock(JobExecution)

        when:
        def result = service.listMindMaps("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * spaceStorage.readMindMap(10L) >> mindMap
        1 * jobRepository.getJobInstance(99L) >> jobInstance
        1 * jobRepository.getLastJobExecution(jobInstance) >> jobExecution
        1 * jobExecution.getStatus() >> BatchStatus.COMPLETED
        result.size() == 1
        result[0].id() == 7L
        result[0].status() == MindMapStatus.DONE
    }

    def "should return spaces for owner"() {
        given:
        def spaces = [
                new Space(1L, "alice", "", "", ""),
                new Space(2L, "alice", "", "", ""),
                new Space(3L, "alice", "", "", ""),
        ]

        when:
        def result = service.getSpaces("alice")

        then:
        1 * spaceStorage.readAll("alice") >> spaces
        result == spaces
    }

    def "should delete space only when it belongs to owner spaces"() {
        given:
        def spaces = [
                new Space(1L, "alice", "", "", ""),
                new Space(2L, "alice", "", "", ""),
        ]

        when:
        service.delete("alice", 2L)

        then:
        1 * spaceStorage.readAll("alice") >> spaces
        1 * spaceStorage.delete(2L)
    }

    def "should not delete space when it is not in owner's spaces"() {
        given:
        def spaces = [
                new Space(1L, "alice", "", "", ""),
        ]

        when:
        service.delete("alice", 2L)

        then:
        1 * spaceStorage.readAll("alice") >> spaces
        0 * spaceStorage.delete(_)
    }
}
