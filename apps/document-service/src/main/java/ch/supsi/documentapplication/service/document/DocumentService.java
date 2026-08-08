package ch.supsi.documentapplication.service.document;

import ch.supsi.documentapplication.dataaccess.*;
import ch.supsi.documentapplication.exception.*;
import ch.supsi.documentapplication.model.*;
import ch.supsi.documentapplication.service.batch.BatchJobRecoveryService;
import ch.supsi.documentapplication.service.batch.JobExecutionResolver;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Service
public class DocumentService extends ch.supsi.documentapplication.service.Service implements IDocumentService {

    public static final String SPACE_ID = "SPACE_ID";

    private final IFileSystemDocumentStorage documentFsStorage;
    private final IDatabaseDocumentStorage databaseDocumentStorage;
    private final IDatabaseDocumentTaskStorage documentTaskStorage;
    private final JobOperator jobOperator;
    private final Job documentJob;
    private final JobRepository jobRepository;
    private final BatchJobRecoveryService batchJobRecoveryService;
    private final DatabasePathStorage databasePathStorage;

    @Autowired
    public DocumentService(
            IFileSystemDocumentStorage documentFsStorage,
            IDatabaseDocumentStorage databaseDocumentStorage,
            IDatabaseDocumentTaskStorage documentTaskStorage,
            IDatabaseSpaceStorage spaceStorage,
            JobOperator asyncJobOperator,
            Job documentJob,
            JobRepository jobRepository,
            BatchJobRecoveryService batchJobRecoveryService,
            DatabasePathStorage databasePathStorage) {
        super(spaceStorage);
        this.documentFsStorage = documentFsStorage;
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.documentTaskStorage = documentTaskStorage;
        this.jobOperator = asyncJobOperator;
        this.documentJob = documentJob;
        this.jobRepository = jobRepository;
        this.batchJobRecoveryService = batchJobRecoveryService;
        this.databasePathStorage = databasePathStorage;
    }

    @Override
    public void createDocument(MultipartFile documentMultipart, String owner, Long spaceId) {
        byte[] documentBytes;
        try {
            documentBytes = documentMultipart.getBytes();
        } catch (Exception e) {
            String message = String.format("Failed to read bytes for file %s", documentMultipart.getOriginalFilename());
            log.error(message, e);
            throw ApplicationException.internalServerError(message);
        }

        Space space = getSpaceOrThrow(owner, spaceId);

        FsDocument fsDocument = FsDocument.create(documentBytes);

        documentFsStorage.create(fsDocument);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("DOCUMENT_ID", fsDocument.getDocumentId().toString())
                .addString("DOCUMENT_NAME", documentMultipart.getOriginalFilename())
                .addLong(SPACE_ID, space.getId())
                .addLong("TRIGGERED_AT", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobOperator.start(documentJob, jobParameters);
        } catch (Exception e) {
            String message = String.format("Failed to start job %s", documentJob.getName());
            log.error(message, e);
            throw new JobFailedToStartException(message);
        }
    }

    @Override
    public List<Document> getDocuments(String owner, Long spaceId) {
        var space = getSpaceOrThrow(owner, spaceId);
        var documents = databaseDocumentStorage.getDocuments(space.getId());

        documents.forEach(document -> document.setTasks(documentTaskStorage.getByDocumentId(document.getId())));

        return documents;
    }

    @Override
    public void moveDocument(String owner, Long spaceId, Long documentId, int orderIndex) {
        var space = getSpaceOrThrow(owner, spaceId);
        var documents = databaseDocumentStorage.getDocuments(space.getId());

        if (documents.stream().noneMatch(document -> document.getId().equals(documentId))) {
            throw NotFoundException.documentNotFoundException(documentId);
        }
        if (orderIndex < 0 || orderIndex >= documents.size()) {
            throw InvalidInputException.invalidDocumentOrderIndex(orderIndex, documents.size());
        }

        databaseDocumentStorage.move(space.getId(), documentId, orderIndex);
    }

    @Override
    public void retryDocument(String owner, Long spaceId, Long documentId) {
        getSpaceOrThrow(owner, spaceId);

        var documents = databaseDocumentStorage.getDocuments(spaceId);
        var document = documents.stream()
                .filter(doc -> doc.getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> NotFoundException.documentNotFoundException(documentId));

        var jobExecution = JobExecutionResolver.getLatestJobExecution(jobRepository, document.getJobId());
        if (jobExecution == null) {
            throw StateConflictException.documentNotRetryable(documentId);
        }

        if (jobExecution.isRunning()) {
            throw StateConflictException.documentStillProcessing(documentId);
        }

        if (!jobExecution.getStatus().isUnsuccessful()) {
            throw StateConflictException.documentNotRetryable(documentId);
        }

        try {
            batchJobRecoveryService.recoverAndRestart(jobExecution);
        } catch (Exception e) {
            String message = String.format("Failed to restart job for document %d", documentId);
            log.error(message, e);
            throw new JobFailedToStartException(message);
        }
    }

    @Override
    public void deleteDocument(String owner, Long spaceId, Long documentId) {
        var space = getSpaceOrThrow(owner, spaceId);
        var documents = databaseDocumentStorage.getDocuments(space.getId());
        var document = documents.stream()
                .filter(doc -> doc.getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> NotFoundException.documentNotFoundException(documentId));

        validateDocumentDeletion(spaceId, documentId);

        JobExecution jobExecution = JobExecutionResolver.getLatestJobExecution(jobRepository, document.getJobId());
        if (jobExecution != null && jobExecution.isRunning()) {
            try {
                jobOperator.stop(jobExecution);
            } catch (Exception e) {
                String message = String.format("Failed to stop job %s", document.getJobId());
                log.error(message, e);
            }
            log.info("Stopped job {} for document {}", document.getJobId(), document.getId());
        }

        databaseDocumentStorage.deleteDocument(spaceId, documentId);
    }

    private void validateDocumentDeletion(Long spaceId, Long documentId) {

        validatePath(spaceId, documentId);
        validateMap(spaceId, documentId);
    }

    private void validateMap(Long spaceId, Long documentId) {
        var mindMap = spaceStorage.readMindMap(spaceId);
        if (mindMap != null && mindMap.getJobId() != null) {
            throw StateConflictException.mindMapAlreadyGeneratedException(documentId);
        }
    }

    private void validatePath(Long spaceId, Long documentId) {
        var path = databasePathStorage.read(spaceId);
        if (path != null) {
            throw StateConflictException.learningPathAlreadyGeneratedException(documentId);
        }
    }
}
