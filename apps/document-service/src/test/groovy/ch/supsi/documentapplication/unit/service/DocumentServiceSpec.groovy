package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentTaskStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage
import ch.supsi.documentapplication.dataaccess.DatabasePathStorage
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage
import ch.supsi.documentapplication.exception.ApplicationException
import ch.supsi.documentapplication.exception.InvalidInputException
import ch.supsi.documentapplication.exception.JobFailedToStartException
import ch.supsi.documentapplication.exception.NotFoundException
import ch.supsi.documentapplication.exception.StateConflictException
import ch.supsi.documentapplication.exception.UnauthorizedException
import ch.supsi.documentapplication.model.Document
import ch.supsi.documentapplication.model.DocumentTask
import ch.supsi.documentapplication.model.FsDocument
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.model.TaskType
import ch.supsi.documentapplication.model.TaskVariant
import ch.supsi.documentapplication.service.batch.BatchJobRecoveryService
import ch.supsi.documentapplication.service.document.DocumentService
import ch.supsi.documentapplication.service.document.DocumentTaskProgressService
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.job.JobInstance
import org.springframework.batch.core.job.parameters.JobParameters
import org.springframework.batch.core.launch.JobOperator
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.StepExecution
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

class DocumentServiceSpec extends Specification {

    private final IFileSystemDocumentStorage documentFsStorage = Mock()
    private final IDatabaseDocumentStorage databaseDocumentStorage = Mock()
    private final IDatabaseDocumentTaskStorage documentTaskStorage = Mock()
    private final DocumentTaskProgressService documentTaskProgressService = Mock()
    private final IDatabaseSpaceStorage spaceStorage = Mock()
    private final JobOperator jobOperator = Mock()
    private final Job pipelineJob = Mock()
    private final JobRepository jobRepository = Mock()
    private final BatchJobRecoveryService batchJobRecoveryService = Mock()
    private final DatabasePathStorage databasePathStorage = Mock()

    private final DocumentService service =
            new DocumentService(documentFsStorage, databaseDocumentStorage, documentTaskStorage, documentTaskProgressService, spaceStorage, jobOperator, pipelineJob, jobRepository, batchJobRecoveryService, databasePathStorage)


    static Space buildSpace(Long id, String owner) {
        return new Space(id, owner, "", "", "")
    }

    Optional<Space> buildOptSpace(Long id, String owner) {
        return Optional.of(buildSpace(id, owner))
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
        thrown(ApplicationException)
        0 * spaceStorage._
        0 * databaseDocumentStorage._
        0 * documentFsStorage._
        0 * jobOperator._
    }


    def "should return documents with tasks for owned space"() {
        given:
        String owner = "alice"
        Long spaceId = 10L
        Optional<Space> space = buildOptSpace(spaceId, owner)
        List<Document> documents = [
                new Document("abc", 1, 2, 0),
                new Document("abc", 2, 3, 1)
        ]
        def task = new DocumentTask("1:OCR", 1L, TaskType.OCR, 1, 1, TaskVariant.SUCCESS)

        when:
        List<Document> result = service.getDocuments(owner, spaceId)

        then:
        1 * spaceStorage.read(spaceId) >> space
        1 * databaseDocumentStorage.getDocuments(spaceId) >> documents
        1 * documentTaskStorage.getByDocumentId(1L) >> [task]
        1 * documentTaskStorage.getByDocumentId(2L) >> []
        1 * jobRepository.getJobInstance(2L) >> null
        1 * jobRepository.getJobInstance(3L) >> null
        0 * documentTaskProgressService.failTask(_, _)
        result == documents
        result[0].tasks == [task]
        result[1].tasks == []
    }

    def "should reconcile unsettled tasks when batch job failed"() {
        given:
        String owner = "alice"
        Long spaceId = 10L
        Long documentId = 1L
        Long jobId = 99L
        Optional<Space> space = buildOptSpace(spaceId, owner)
        Document document = new Document("doc.pdf", documentId, jobId, 0)
        def existingTasks = [
                new DocumentTask("1:SEGMENTATION", documentId, TaskType.SEGMENTATION, 1, 1, TaskVariant.SUCCESS),
                new DocumentTask("1:OCR", documentId, TaskType.OCR, 1, 1, TaskVariant.SUCCESS),
                new DocumentTask("1:SUMMARIZATION", documentId, TaskType.SUMMARIZATION, 1, 1, TaskVariant.SUCCESS),
        ]
        def reconciledTasks = existingTasks + [
                new DocumentTask("1:TOPIC", documentId, TaskType.TOPIC, 0, 1, TaskVariant.ERROR)
        ]
        JobInstance jobInstance = Mock()
        JobExecution jobExecution = Mock()
        StepExecution failedStep = Mock()

        when:
        List<Document> result = service.getDocuments(owner, spaceId)

        then:
        1 * spaceStorage.read(spaceId) >> space
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [document]
        1 * documentTaskStorage.getByDocumentId(documentId) >> existingTasks
        1 * jobRepository.getJobInstance(jobId) >> jobInstance
        1 * jobRepository.getLastJobExecution(jobInstance) >> jobExecution
        1 * jobExecution.isRunning() >> false
        _ * jobExecution.getStatus() >> BatchStatus.FAILED
        1 * jobExecution.getStepExecutions() >> [failedStep]
        _ * failedStep.getStatus() >> BatchStatus.FAILED
        1 * failedStep.getStepName() >> "TopicExtractionTasklet\$\$SpringCGLIB\$\$0"
        1 * documentTaskProgressService.failTask(documentId, TaskType.TOPIC)
        1 * documentTaskStorage.getByDocumentId(documentId) >> reconciledTasks
        result[0].tasks == reconciledTasks
    }

    def "should not reconcile when document tasks already have ERROR"() {
        given:
        String owner = "alice"
        Long spaceId = 10L
        Long documentId = 1L
        Optional<Space> space = buildOptSpace(spaceId, owner)
        Document document = new Document("doc.pdf", documentId, 99L, 0)
        def tasks = [
                new DocumentTask("1:TOPIC", documentId, TaskType.TOPIC, 0, 1, TaskVariant.ERROR)
        ]

        when:
        List<Document> result = service.getDocuments(owner, spaceId)

        then:
        1 * spaceStorage.read(spaceId) >> space
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [document]
        1 * documentTaskStorage.getByDocumentId(documentId) >> tasks
        0 * jobRepository._
        0 * documentTaskProgressService.failTask(_, _)
        result[0].tasks == tasks
    }

    def "should not reconcile when batch job is still running"() {
        given:
        String owner = "alice"
        Long spaceId = 10L
        Long documentId = 1L
        Long jobId = 99L
        Optional<Space> space = buildOptSpace(spaceId, owner)
        Document document = new Document("doc.pdf", documentId, jobId, 0)
        def tasks = [
                new DocumentTask("1:OCR", documentId, TaskType.OCR, 1, 1, TaskVariant.SUCCESS)
        ]
        JobInstance jobInstance = Mock()
        JobExecution jobExecution = Mock()

        when:
        List<Document> result = service.getDocuments(owner, spaceId)

        then:
        1 * spaceStorage.read(spaceId) >> space
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [document]
        1 * documentTaskStorage.getByDocumentId(documentId) >> tasks
        1 * jobRepository.getJobInstance(jobId) >> jobInstance
        1 * jobRepository.getLastJobExecution(jobInstance) >> jobExecution
        1 * jobExecution.isRunning() >> true
        0 * documentTaskProgressService.failTask(_, _)
        result[0].tasks == tasks
    }

    def "should not reconcile when batch job completed successfully"() {
        given:
        String owner = "alice"
        Long spaceId = 10L
        Long documentId = 1L
        Long jobId = 99L
        Optional<Space> space = buildOptSpace(spaceId, owner)
        Document document = new Document("doc.pdf", documentId, jobId, 0)
        def tasks = [
                new DocumentTask("1:OCR", documentId, TaskType.OCR, 1, 1, TaskVariant.SUCCESS)
        ]
        JobInstance jobInstance = Mock()
        JobExecution jobExecution = Mock()

        when:
        List<Document> result = service.getDocuments(owner, spaceId)

        then:
        1 * spaceStorage.read(spaceId) >> space
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [document]
        1 * documentTaskStorage.getByDocumentId(documentId) >> tasks
        1 * jobRepository.getJobInstance(jobId) >> jobInstance
        1 * jobRepository.getLastJobExecution(jobInstance) >> jobExecution
        1 * jobExecution.isRunning() >> false
        1 * jobExecution.getStatus() >> BatchStatus.COMPLETED
        0 * documentTaskProgressService.failTask(_, _)
        result[0].tasks == tasks
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

    def "should move document for owned space"() {
        given:
        def documents = [
                new Document("first.pdf", 1L, 11L, 0),
                new Document("second.pdf", 2L, 12L, 1)
        ]

        when:
        service.moveDocument("alice", 10L, 2L, 0)

        then:
        1 * spaceStorage.read(10L) >> buildOptSpace(10L, "alice")
        1 * databaseDocumentStorage.getDocuments(10L) >> documents
        1 * databaseDocumentStorage.move(10L, 2L, 0)
    }

    def "should reject move when document is not in space"() {
        when:
        service.moveDocument("alice", 10L, 42L, 0)

        then:
        1 * spaceStorage.read(10L) >> buildOptSpace(10L, "alice")
        1 * databaseDocumentStorage.getDocuments(10L) >> [
                new Document("first.pdf", 1L, 11L, 0)
        ]
        thrown(NotFoundException)
        0 * databaseDocumentStorage.move(_, _, _)
    }

    def "should reject move outside document bounds"() {
        when:
        service.moveDocument("alice", 10L, 1L, 1)

        then:
        1 * spaceStorage.read(10L) >> buildOptSpace(10L, "alice")
        1 * databaseDocumentStorage.getDocuments(10L) >> [
                new Document("first.pdf", 1L, 11L, 0)
        ]
        thrown(InvalidInputException)
        0 * databaseDocumentStorage.move(_, _, _)
    }

    def "should reject move when requester is not owner"() {
        when:
        service.moveDocument("alice", 10L, 1L, 0)

        then:
        1 * spaceStorage.read(10L) >> buildOptSpace(10L, "bob")
        thrown(UnauthorizedException)
        0 * databaseDocumentStorage._
    }

    def "should restart failed document job"() {
        given:
        String owner = "alice"
        Long spaceId = 10L
        Long documentId = 1L
        Long jobId = 99L
        Optional<Space> space = buildOptSpace(spaceId, owner)
        Document document = new Document("doc.pdf", documentId, jobId, 0)
        JobInstance jobInstance = Mock()
        JobExecution jobExecution = Mock()

        when:
        service.retryDocument(owner, spaceId, documentId)

        then:
        1 * spaceStorage.read(spaceId) >> space
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [document]
        1 * jobRepository.getJobInstance(jobId) >> jobInstance
        1 * jobRepository.getLastJobExecution(jobInstance) >> jobExecution
        1 * jobExecution.isRunning() >> false
        1 * jobExecution.getStatus() >> BatchStatus.FAILED
        1 * batchJobRecoveryService.recoverAndRestart(jobExecution)
    }

    def "should throw not found when retrying unknown document"() {
        given:
        String owner = "alice"
        Long spaceId = 10L
        Optional<Space> space = buildOptSpace(spaceId, owner)

        when:
        service.retryDocument(owner, spaceId, 42L)

        then:
        1 * spaceStorage.read(spaceId) >> space
        1 * databaseDocumentStorage.getDocuments(spaceId) >> []
        thrown(NotFoundException)
        0 * jobOperator._
    }

    def "should throw conflict when retrying a running document job"() {
        given:
        String owner = "alice"
        Long spaceId = 10L
        Long documentId = 1L
        Optional<Space> space = buildOptSpace(spaceId, owner)
        Document document = new Document("doc.pdf", documentId, 99L, 0)
        JobInstance jobInstance = Mock()
        JobExecution jobExecution = Mock()

        when:
        service.retryDocument(owner, spaceId, documentId)

        then:
        1 * spaceStorage.read(spaceId) >> space
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [document]
        1 * jobRepository.getJobInstance(99L) >> jobInstance
        1 * jobRepository.getLastJobExecution(jobInstance) >> jobExecution
        1 * jobExecution.isRunning() >> true
        thrown(StateConflictException)
        0 * jobOperator._
    }

    def "should throw conflict when retrying a completed document job"() {
        given:
        String owner = "alice"
        Long spaceId = 10L
        Long documentId = 1L
        Optional<Space> space = buildOptSpace(spaceId, owner)
        Document document = new Document("doc.pdf", documentId, 99L, 0)
        JobInstance jobInstance = Mock()
        JobExecution jobExecution = Mock()

        when:
        service.retryDocument(owner, spaceId, documentId)

        then:
        1 * spaceStorage.read(spaceId) >> space
        1 * databaseDocumentStorage.getDocuments(spaceId) >> [document]
        1 * jobRepository.getJobInstance(99L) >> jobInstance
        1 * jobRepository.getLastJobExecution(jobInstance) >> jobExecution
        1 * jobExecution.isRunning() >> false
        1 * jobExecution.getStatus() >> BatchStatus.COMPLETED
        thrown(StateConflictException)
        0 * jobOperator._
    }
}