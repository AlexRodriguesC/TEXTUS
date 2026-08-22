package ch.supsi.documentapplication.service.document;

import ch.supsi.documentapplication.dataaccess.*;
import ch.supsi.documentapplication.exception.*;
import ch.supsi.documentapplication.model.*;
import ch.supsi.documentapplication.service.async.documentpipeline.DocumentPipelineTaskType;
import ch.supsi.documentapplication.service.batch.BatchJobRecoveryService;
import ch.supsi.documentapplication.service.batch.JobExecutionResolver;
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Service
public class DocumentService extends ch.supsi.documentapplication.service.Service implements IDocumentService {

    public static final String SPACE_ID = "SPACE_ID";

    private static final List<TaskType> PIPELINE_TASK_TYPES =
            Arrays.asList(TaskType.SEGMENTATION, TaskType.OCR, TaskType.SUMMARIZATION, TaskType.TOPIC);

    private final IFileSystemDocumentStorage documentFsStorage;
    private final IDatabaseDocumentStorage databaseDocumentStorage;
    private final IDatabaseDocumentTaskStorage documentTaskStorage;
    private final DocumentTaskProgressService documentTaskProgressService;
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
            DocumentTaskProgressService documentTaskProgressService,
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
        this.documentTaskProgressService = documentTaskProgressService;
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

        documents.forEach(document -> {
            List<DocumentTask> tasks = documentTaskStorage.getByDocumentId(document.getId());
            tasks = reconcileFailedDocumentTasks(document, tasks);
            document.setTasks(tasks);
        });

        return documents;
    }

    /**
     * When the Batch job has already failed but no task row has {@code ERROR}
     * (e.g. startTask was rolled back with the failing chunk), persist ERROR so
     * the UI stops showing in-progress.
     */
    private List<DocumentTask> reconcileFailedDocumentTasks(Document document, List<DocumentTask> tasks) {
        if (!isUnsettled(tasks)) {
            return tasks;
        }

        JobExecution jobExecution = JobExecutionResolver.getLatestJobExecution(jobRepository, document.getJobId());
        if (jobExecution == null
                || jobExecution.isRunning()
                || !jobExecution.getStatus().isUnsuccessful()) {
            return tasks;
        }

        TaskType failedType = resolveFailedPipelineTaskType(jobExecution);
        if (failedType == null) {
            return tasks;
        }

        log.warn(
                "Reconciling stuck document tasks with Batch failure: documentId={}, taskType={}, jobStatus={}",
                document.getId(),
                failedType,
                jobExecution.getStatus());
        documentTaskProgressService.failTask(document.getId(), failedType);
        return documentTaskStorage.getByDocumentId(document.getId());
    }

    private static boolean isUnsettled(List<DocumentTask> tasks) {
        return !isFailed(tasks) && !isDone(tasks);
    }

    private static boolean isFailed(List<DocumentTask> tasks) {
        return tasks.stream().anyMatch(task -> task.getVariant() == TaskVariant.ERROR);
    }

    private static boolean isDone(List<DocumentTask> tasks) {
        return PIPELINE_TASK_TYPES.stream().allMatch(type -> tasks.stream()
                .anyMatch(task -> task.getType() == type && task.getVariant() == TaskVariant.SUCCESS));
    }

    private static TaskType resolveFailedPipelineTaskType(JobExecution jobExecution) {
        return jobExecution.getStepExecutions().stream()
                .filter(DocumentService::isFailedStep)
                .map(StepExecution::getStepName)
                .map(DocumentPipelineTaskType::fromStepName)
                .filter(type -> type != null)
                .findFirst()
                .orElse(null);
    }

    private static boolean isFailedStep(StepExecution stepExecution) {
        if (stepExecution.getStatus().isUnsuccessful()) {
            return true;
        }
        ExitStatus exitStatus = stepExecution.getExitStatus();
        return exitStatus != null && ExitStatus.FAILED.getExitCode().equals(exitStatus.getExitCode());
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
