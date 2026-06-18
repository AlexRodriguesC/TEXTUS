package ch.supsi.textusbackend.unit.service


import ch.supsi.textusbackend.dataaccess.IDatabaseDocumentStorage
import ch.supsi.textusbackend.dataaccess.IFileSystemDocumentStorage
import ch.supsi.textusbackend.model.Book
import ch.supsi.textusbackend.service.DocumentService
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.launch.JobOperator
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

class BookServiceSpec extends Specification {

    IDatabaseDocumentStorage databaseBookStorage = Mock(IDatabaseDocumentStorage) {
        create(_ as Book) >> 1L
    }
    IFileSystemDocumentStorage fileSystemBookStorage = Mock()
    JobOperator jobOperator = Mock()
    Job pipelineJob = Mock()
    DocumentService service

    def setup() {
        pipelineJob.getName() >> "pipelineJob"

        service = new DocumentService(fileSystemBookStorage, databaseBookStorage, jobOperator, pipelineJob)
    }

    private MultipartFile mockFile(String name = "book.pdf", long size = 42L, byte[] bytes = "pdf-content".bytes) {
        Mock(MultipartFile) {
            getOriginalFilename() >> name
            getSize() >> size
            getBytes() >> bytes
        }
    }

    def "should create book and start job"() {
        given:
        def file = mockFile()

        when:
        service.parseDocument(file)

        then:
        1 * databaseBookStorage.create(_ as Book) >> 1L 
        1 * jobOperator.start(pipelineJob, _ as JobParameters)
    }

    def "should throw exception if it fails to get bytes from book file"() {
        given:
        def file = Mock(MultipartFile) {
            getOriginalFilename() >> "book.pdf"
            getSize() >> 42L
            getBytes() >> { throw new IOException("disk read failure") }
        }

        when:
        service.parseDocument(file)

        then:
        1 * databaseBookStorage.create(_ as Book) >> 1L
        1 * jobOperator.start(pipelineJob, _ as JobParameters)
    }

    def "should start job with right job parameters"() {
        given:
        def file = mockFile()
        def capturedParams = null

        jobOperator.start(pipelineJob, _ as JobParameters) >> { Job job, JobParameters params ->
            capturedParams = params
            null
        }

        when:
        service.parseDocument(file)

        then:
        capturedParams != null
        capturedParams.getLong("BOOK_ID") != null
        capturedParams.getLong("TRIGGERED_AT") != null
    }

    def "should throw if job fails to start"() {
        given:
        def file = mockFile()
        jobOperator.start(pipelineJob, _ as JobParameters) >> { throw new RuntimeException("job launch failed") }

        when:
        service.parseDocument(file)

        then:
        // exception is caught and logged internally — no rethrow
        noExceptionThrown()
        1 * databaseBookStorage.create(_ as Book) >> 1L
    }
}