package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage
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
    def documentStorage = Mock(IDatabaseDocumentStorage)
    def jobOperator = Mock(JobOperator)
    def mindMapJob = Mock(Job)

    def service = new SpaceService(spaceStorage, documentStorage, jobOperator, mindMapJob)

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

    def "should return mind map status"() {
        given:
        def mindMap = Mock(MindMap)
        mindMap.getMindMapStatus() >> MindMapStatus.DONE
        def space = Optional.of(new Space(10L, "alice", "", "", ""))

        when:
        def result = service.getMindMapStatus("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * spaceStorage.readMindMap(10L) >> mindMap
        result == MindMapStatus.DONE
    }

    def "should throw not found when mind map is null for status"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))

        when:
        service.getMindMapStatus("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * spaceStorage.readMindMap(10L) >> null
        def e = thrown(NotFoundException)
        e.message
    }

    def "should create mind map and start job"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))

        when:
        service.createMindMap("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * jobOperator.start(mindMapJob, _ as JobParameters)
        noExceptionThrown()
    }

    def "should throw domain exception when mind map job fails"() {
        given:
        def space = Optional.of(new Space(10L, "alice", "", "", ""))

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