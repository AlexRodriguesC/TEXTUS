package ch.supsi.documentapplication.unit.service


import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage
import ch.supsi.documentapplication.exception.DocumentDomainException
import ch.supsi.documentapplication.service.DocumentService
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.launch.JobOperator
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

class DocumentServiceSpec extends Specification {

    IDatabaseDocumentStorage databaseDocumentStorage = Mock(IDatabaseDocumentStorage)
    IFileSystemDocumentStorage fileSystemDocumentStorage = Mock()
    JobOperator jobOperator = Mock()
    Job pipelineJob = Mock()
    DocumentService service

    def setup() {
        pipelineJob.getName() >> "pipelineJob"

        service = new DocumentService(fileSystemDocumentStorage, databaseDocumentStorage, jobOperator, pipelineJob)
    }

    private MultipartFile mockFile(String name = "document.pdf", long size = 42L, byte[] bytes = "pdf-content".bytes) {
        Mock(MultipartFile) {
            getOriginalFilename() >> name
            getSize() >> size
            getBytes() >> bytes
        }
    }

    def "should start job"() {
        given:
        def file = mockFile()

        when:
        service.parseDocument(file)

        then:
        1 * jobOperator.start(pipelineJob, _ as JobParameters)
    }

    def "should throw exception if it fails to get bytes from document file"() {
        given:
        def file = Mock(MultipartFile) {
            getOriginalFilename() >> "document.pdf"
            getSize() >> 42L
            getBytes() >> { throw new IOException("disk read failure") }
        }

        when:
        service.parseDocument(file)

        then:
        thrown(DocumentDomainException)
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
        capturedParams.getString("DOCUMENT_ID") != null
        capturedParams.getLong("TRIGGERED_AT") != null
    }

    def "should throw if job fails to start"() {
        given:
        def file = mockFile()
        jobOperator.start(pipelineJob, _ as JobParameters) >> { throw new RuntimeException("job launch failed") }

        when:
        service.parseDocument(file)

        then:
        thrown(DocumentDomainException)
    }
}