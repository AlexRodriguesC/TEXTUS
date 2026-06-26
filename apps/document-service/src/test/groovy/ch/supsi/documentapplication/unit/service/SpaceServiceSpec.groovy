package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage
import ch.supsi.documentapplication.exception.DocumentDomainException
import ch.supsi.documentapplication.exception.NotFoundException
import ch.supsi.documentapplication.exception.UnauthorizedException
import ch.supsi.documentapplication.model.MindMap
import ch.supsi.documentapplication.model.MindMapStatus
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.service.SpaceService
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.launch.JobOperator
import spock.lang.Specification

class SpaceServiceSpec extends Specification {

    def spaceStorage = Mock(IDatabaseSpaceStorage)
    def jobOperator = Mock(JobOperator)
    def mindMapJob = Mock(Job)

    def service = new SpaceService(spaceStorage, jobOperator, mindMapJob)

    def "should return mind map for authorized user"() {
        given:
        def space = Space.builder().id(10L).owner("alice").build()
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
        1 * spaceStorage.read(10L) >> null
        def e = thrown(NotFoundException)
        e.message
        0 * spaceStorage.readMindMap(_)
    }

    def "should throw unauthorized when requester is not owner of mind map space"() {
        given:
        def space = Space.builder().id(10L).owner("bob").build()

        when:
        service.getMindMap("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        def e = thrown(UnauthorizedException)
        e.message
        0 * spaceStorage.readMindMap(_)
    }

    def "should return mind map status"() {
        given:
        def mindMap = Mock(MindMap)
        mindMap.getMindMapStatus() >> MindMapStatus.DONE
        def space = Space.builder().id(10L).owner("alice").build()

        when:
        def result = service.getMindMapStatus("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * spaceStorage.readMindMap(10L) >> mindMap
        result == MindMapStatus.DONE
    }

    def "should throw not found when mind map is null for status"() {
        given:
        def space = Space.builder().id(10L).owner("alice").build()

        when:
        service.getMindMapStatus("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * spaceStorage.readMindMap(10L) >> null
        def e = thrown(NotFoundException)
        e.message
    }

    def "should update space using existing values when name and description are empty"() {
        given:
        def space = Space.builder().id(10L).owner("alice").name("old").description("desc").build()

        when:
        service.update("alice", 10L, "", "")

        then:
        1 * spaceStorage.read(10L) >> space
        1 * spaceStorage.update(10L, "old", "desc")
    }

    def "should update space using provided values"() {
        given:
        def space = Space.builder().id(10L).owner("alice").name("old").description("desc").build()

        when:
        service.update("alice", 10L, "new", "new desc")

        then:
        1 * spaceStorage.read(10L) >> space
        1 * spaceStorage.update(10L, "new", "new desc")
    }

    def "should create mind map and start job"() {
        given:
        def space = Space.builder().id(10L).owner("alice").build()

        when:
        service.createMindMap("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * jobOperator.start(mindMapJob, _ as JobParameters)
        noExceptionThrown()
    }

    def "should throw domain exception when mind map job fails"() {
        given:
        def space = Space.builder().id(10L).owner("alice").build()

        when:
        service.createMindMap("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * jobOperator.start(mindMapJob, _ as JobParameters) >> { throw new RuntimeException("boom") }
        def e = thrown(DocumentDomainException)
        e.message == "Failed to start asynchronous job"
    }

    def "should return spaces for owner"() {
        given:
        def spaces = [
                Space.builder().id(1L).owner("alice").build(),
                Space.builder().id(2L).owner("alice").build()
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
                Space.builder().id(1L).owner("alice").build(),
                Space.builder().id(2L).owner("alice").build()
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
                Space.builder().id(1L).owner("alice").build()
        ]

        when:
        service.delete("alice", 2L)

        then:
        1 * spaceStorage.readAll("alice") >> spaces
        0 * spaceStorage.delete(_)
    }
}