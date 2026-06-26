package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage
import ch.supsi.documentapplication.exception.DocumentDomainException
import ch.supsi.documentapplication.exception.NotFoundException
import ch.supsi.documentapplication.exception.UnauthorizedException
import ch.supsi.documentapplication.model.Document
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.service.document.DocumentService
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.launch.JobOperator
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

class DocumentServiceSpec extends Specification {

    def documentStorage = Mock(IFileSystemDocumentStorage)
    def spaceStorage = Mock(IDatabaseSpaceStorage)
    def jobOperator = Mock(JobOperator)
    def pipelineJob = Mock(Job)

    def service = new DocumentService(documentStorage, spaceStorage, jobOperator, pipelineJob)

    def "should create document and start pipeline job"() {
        given:
        def file = new MockMultipartFile("document", "doc.txt", "text/plain", "hello".bytes)
        def space = Space.builder().id(10L).owner("alice").build()

        when:
        service.createDocument(file, "alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * documentStorage.create(_ as Document) >> { Document d ->
            assert d != null
            assert d.getDocumentId() != null
        }
        1 * jobOperator.start(pipelineJob, _ as JobParameters)
        noExceptionThrown()
    }

    def "should throw not found when space does not exist"() {
        given:
        def file = new MockMultipartFile("document", "doc.txt", "text/plain", "hello".bytes)

        when:
        service.createDocument(file, "alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> null
        def e = thrown(NotFoundException)
        e.message
        0 * documentStorage._
        0 * jobOperator._
    }

    def "should throw unauthorized when owner does not match"() {
        given:
        def file = new MockMultipartFile("document", "doc.txt", "text/plain", "hello".bytes)
        def space = Space.builder().id(10L).owner("bob").build()

        when:
        service.createDocument(file, "alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        def e = thrown(UnauthorizedException)
        e.message
        0 * documentStorage._
        0 * jobOperator._
    }

    def "should throw domain exception when file bytes cannot be read"() {
        given:
        def file = Stub(MultipartFile)
        file.getOriginalFilename() >> "broken.txt"
        file.getBytes() >> { throw new IOException("boom") }

        when:
        service.createDocument(file, "alice", 10L)

        then:
        def e = thrown(DocumentDomainException)
        e.message == "Failed to get file bytes"
        0 * spaceStorage._
        0 * documentStorage._
        0 * jobOperator._
    }

    def "should throw domain exception when job start fails"() {
        given:
        def file = new MockMultipartFile("document", "doc.txt", "text/plain", "hello".bytes)
        def space = Space.builder().id(10L).owner("alice").build()

        when:
        service.createDocument(file, "alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        1 * documentStorage.create(_ as Document)
        1 * jobOperator.start(pipelineJob, _ as JobParameters) >> { throw new RuntimeException("job failed") }
        def e = thrown(DocumentDomainException)
        e.message == "Failed to start asynchronous job"
    }
}