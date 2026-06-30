package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage
import ch.supsi.documentapplication.exception.DocumentDomainException
import ch.supsi.documentapplication.exception.NotFoundException
import ch.supsi.documentapplication.exception.UnauthorizedException
import ch.supsi.documentapplication.model.Document
import ch.supsi.documentapplication.model.DocumentStatus
import ch.supsi.documentapplication.model.FsDocument
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.service.document.DocumentService
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.launch.JobOperator
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

class DocumentServiceSpec extends Specification {

    private final IFileSystemDocumentStorage documentFsStorage = Mock()
    private final IDatabaseDocumentStorage databaseDocumentStorage = Mock()
    private final IDatabaseSpaceStorage spaceStorage = Mock()
    private final JobOperator jobOperator = Mock()
    private final Job pipelineJob = Mock()

    private final DocumentService service =
            new DocumentService(documentFsStorage, databaseDocumentStorage, spaceStorage, jobOperator, pipelineJob)


    Space buildSpace(Long id, String owner) {
        return new Space(id, owner, "", "", "")
    }

    Optional<Space> buildOptSpace(Long id, String owner) {
        return Optional.of(buildSpace(id, owner))
    }

    def "should create document, persist it, and start pipeline job"() {
        given:
        String owner = "alice"
        Long spaceId = 10L
        byte[] bytes = "hello".bytes
        MockMultipartFile file = new MockMultipartFile("document", "doc.txt", "text/plain", bytes)
        Optional<Space> space = buildOptSpace(spaceId, owner)
        Long databaseDocId = 99L

        when:
        service.createDocument(file, owner, spaceId)

        then:
        1 * spaceStorage.read(spaceId) >> space
        1 * databaseDocumentStorage.create(spaceId) >> databaseDocId
        1 * documentFsStorage.create({ FsDocument document ->
            document != null
        })
        1 * jobOperator.start(pipelineJob, _ as JobParameters)
        0 * _
    }

    def "should throw not found when space is missing"() {
        given:
        String owner = "alice"
        Long spaceId = 10L
        MockMultipartFile file = new MockMultipartFile("document", "doc.txt", "text/plain", "hello".bytes)

        when:
        service.createDocument(file, owner, spaceId)

        then:
        1 * spaceStorage.read(spaceId) >> Optional.empty()
        thrown(NotFoundException)
        0 * databaseDocumentStorage._
        0 * documentFsStorage._
        0 * jobOperator._
    }

    def "should throw unauthorized when owner does not match"() {
        given:
        String owner = "alice"
        Long spaceId = 10L
        MockMultipartFile file = new MockMultipartFile("document", "doc.txt", "text/plain", "hello".bytes)
        Optional<Space> space = buildOptSpace(spaceId, "bob")

        when:
        service.createDocument(file, owner, spaceId)

        then:
        1 * spaceStorage.read(spaceId) >> space
        thrown(UnauthorizedException)
        0 * databaseDocumentStorage._
        0 * documentFsStorage._
        0 * jobOperator._
    }

    def "should throw domain exception when file bytes cannot be read"() {
        given:
        String owner = "alice"
        Long spaceId = 10L
        def file = Stub(MultipartFile) {
            getOriginalFilename() >> "doc.txt"
            getBytes() >> { throw new IOException("boom") }
        }

        when:
        service.createDocument(file, owner, spaceId)

        then:
        DocumentDomainException e = thrown(DocumentDomainException)
        e.message == "Failed to get file bytes"
        0 * spaceStorage._
        0 * databaseDocumentStorage._
        0 * documentFsStorage._
        0 * jobOperator._
    }

    def "should throw domain exception when job start fails"() {
        given:
        String owner = "alice"
        Long spaceId = 10L
        MockMultipartFile file = new MockMultipartFile("document", "doc.txt", "text/plain", "hello".bytes)
        Optional<Space> space = buildOptSpace(spaceId, owner)
        Long databaseDocId = 99L

        when:
        service.createDocument(file, owner, spaceId)

        then:
        1 * spaceStorage.read(spaceId) >> space
        1 * databaseDocumentStorage.create(spaceId) >> databaseDocId
        1 * documentFsStorage.create(_ as FsDocument)
        1 * jobOperator.start(pipelineJob, _ as JobParameters) >> { throw new RuntimeException("boom") }
        DocumentDomainException e = thrown(DocumentDomainException)
        e.message == "Failed to start asynchronous job"
    }

    def "should return documents for owned space"() {
        given:
        String owner = "alice"
        Long spaceId = 10L
        Optional<Space> space = buildOptSpace(spaceId, owner)
        List<Document> documents = [
                new Document(1, DocumentStatus.DONE),
                new Document(2, DocumentStatus.PROCESSING)
        ]

        when:
        List<Document> result = service.getDocuments(owner, spaceId)

        then:
        1 * spaceStorage.read(spaceId) >> space
        1 * databaseDocumentStorage.getDocuments(spaceId) >> documents
        result == documents
    }

    def "should throw not found when space is missing"() {
        when:
        service.getDocuments("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> Optional.empty()
        thrown(NotFoundException)
        0 * databaseDocumentStorage._
    }

    def "should throw unauthorized when requester is not owner"() {
        given:
        Optional<Space> space = buildOptSpace(10L, "bob")

        when:
        service.getDocuments("alice", 10L)

        then:
        1 * spaceStorage.read(10L) >> space
        thrown(UnauthorizedException)
        0 * databaseDocumentStorage._
    }
}