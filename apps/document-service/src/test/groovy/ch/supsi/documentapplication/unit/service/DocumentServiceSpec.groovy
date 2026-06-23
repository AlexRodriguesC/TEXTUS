package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage
import ch.supsi.documentapplication.exception.DocumentDomainException
import ch.supsi.documentapplication.model.Document
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.service.document.DocumentService
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.launch.JobOperator
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

class DocumentServiceSpec extends Specification {

    IFileSystemDocumentStorage documentStorage = Mock()
    IDatabaseSpaceStorage spaceStorage = Mock()
    JobOperator jobOperator = Mock()
    Job pipelineJob = Mock()

    DocumentService service

    def setup() {
        pipelineJob.getName() >> "pipelineJob"
        service = new DocumentService(documentStorage, spaceStorage, jobOperator, pipelineJob)
    }

    def "should store document and start job when space exists"() {
        given:
        def file = Mock(MultipartFile) {
            getBytes() >> "content".bytes
            getOriginalFilename() >> "doc.pdf"
        }
        spaceStorage.read(1L) >> new Space()

        when:
        service.createDocument(file, 1L)

        then:
        1 * documentStorage.create(_ as Document)
        1 * jobOperator.start(pipelineJob, { it.getString("SPACE_ID") == "1" })
    }

    def "should pass correct SPACE_ID job parameter"() {
        given:
        def file = Mock(MultipartFile) {
            getBytes() >> "data".bytes
            getOriginalFilename() >> "file.pdf"
        }
        spaceStorage.read(99L) >> new Space()

        when:
        service.createDocument(file, 99L)

        then:
        1 * jobOperator.start(pipelineJob, {
            it.getString("SPACE_ID") == "99"
        })
    }

    def "should pass non-null DOCUMENT_ID job parameter"() {
        given:
        def file = Mock(MultipartFile) {
            getBytes() >> "data".bytes
            getOriginalFilename() >> "file.pdf"
        }
        spaceStorage.read(1L) >> new Space()

        when:
        service.createDocument(file, 1L)

        then:
        1 * jobOperator.start(pipelineJob, {
            it.getString("DOCUMENT_ID") != null
        })
    }

    def "should pass TRIGGERED_AT job parameter"() {
        given:
        def before = System.currentTimeMillis()
        def file = Mock(MultipartFile) {
            getBytes() >> "data".bytes
            getOriginalFilename() >> "file.pdf"
        }
        spaceStorage.read(1L) >> new Space()

        when:
        service.createDocument(file, 1L)

        then:
        1 * jobOperator.start(pipelineJob, {
            it.getLong("TRIGGERED_AT") >= before
        })
    }

    def "should throw DocumentDomainException when space is not found"() {
        given:
        def file = Mock(MultipartFile) {
            getBytes() >> "content".bytes
            getOriginalFilename() >> "doc.pdf"
        }
        spaceStorage.read(42L) >> null

        when:
        service.createDocument(file, 42L)

        then:
        def ex = thrown(DocumentDomainException)
        ex.message == "Space not found"
        0 * documentStorage.create(_)
        0 * jobOperator.start(_, _)
    }

    def "should throw DocumentDomainException when file bytes cannot be read"() {
        given:
        def file = Mock(MultipartFile) {
            getBytes() >> { throw new IOException("disk error") }
            getOriginalFilename() >> "bad.pdf"
        }

        when:
        service.createDocument(file, 1L)

        then:
        def ex = thrown(DocumentDomainException)
        ex.message == "Failed to get file bytes"
        0 * spaceStorage.read(_)
        0 * documentStorage.create(_)
        0 * jobOperator.start(_, _)
    }

    def "should wrap original exception as cause when file bytes cannot be read"() {
        given:
        def cause = new IOException("disk error")
        def file = Mock(MultipartFile) {
            getBytes() >> { throw cause }
            getOriginalFilename() >> "bad.pdf"
        }

        when:
        service.createDocument(file, 1L)

        then:
        def ex = thrown(DocumentDomainException)
        ex.cause == cause
    }

    def "should throw DocumentDomainException when job fails to start"() {
        given:
        def file = Mock(MultipartFile) {
            getBytes() >> "content".bytes
            getOriginalFilename() >> "doc.pdf"
        }
        spaceStorage.read(1L) >> new Space()
        jobOperator.start(_, _) >> { throw new RuntimeException("batch down") }

        when:
        service.createDocument(file, 1L)

        then:
        def ex = thrown(DocumentDomainException)
        ex.message == "Failed to start job"
        1 * documentStorage.create(_ as Document)   // document was stored before job failed
    }

    def "should wrap original exception as cause when job fails to start"() {
        given:
        def cause = new RuntimeException("batch down")
        def file = Mock(MultipartFile) {
            getBytes() >> "content".bytes
            getOriginalFilename() >> "doc.pdf"
        }
        spaceStorage.read(1L) >> new Space()
        jobOperator.start(_, _) >> { throw cause }

        when:
        service.createDocument(file, 1L)

        then:
        def ex = thrown(DocumentDomainException)
        ex.cause == cause
    }

    def "should read space before creating document"() {
        given:
        def file = Mock(MultipartFile) {
            getBytes() >> "content".bytes
            getOriginalFilename() >> "doc.pdf"
        }
        def callOrder = []
        spaceStorage.read(1L) >> { callOrder << "readSpace"; new Space() }
        documentStorage.create(_ as Document) >> { callOrder << "createDoc" }

        when:
        service.createDocument(file, 1L)

        then:
        callOrder == ["readSpace", "createDoc"]
        1 * jobOperator.start(_, _)
    }
}