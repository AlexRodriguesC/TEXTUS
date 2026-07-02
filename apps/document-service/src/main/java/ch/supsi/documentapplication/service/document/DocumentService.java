package ch.supsi.documentapplication.service.document;

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage;
import ch.supsi.documentapplication.exception.*;
import ch.supsi.documentapplication.model.*;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Service
public class DocumentService implements IDocumentService {

    public static final String SPACE_ID = "SPACE_ID";

    private final IFileSystemDocumentStorage documentFsStorage;
    private final IDatabaseDocumentStorage databaseDocumentStorage;
    private final IDatabaseSpaceStorage spaceStorage;
    private final JobOperator jobOperator;
    private final Job documentJob;
    private final JobRepository jobRepository;

    @Autowired
    public DocumentService(
            IFileSystemDocumentStorage documentFsStorage,
            IDatabaseDocumentStorage databaseDocumentStorage,
            IDatabaseSpaceStorage spaceStorage,
            JobOperator asyncJobOperator,
            Job documentJob,
            JobRepository jobRepository) {
        this.documentFsStorage = documentFsStorage;
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.spaceStorage = spaceStorage;
        this.jobOperator = asyncJobOperator;
        this.documentJob = documentJob;
        this.jobRepository = jobRepository;
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
            var jobId = document.getJobId();
            var jobExecution = jobRepository.getJobExecution(jobId);
            if (jobExecution == null) {
                document.setStatus(DocumentStatus.FAILED);
                return;
            }

            var status = jobExecution.getStatus();
            var docStatus = status.isRunning()
                    ? DocumentStatus.PROCESSING
                    : status.isUnsuccessful() ? DocumentStatus.FAILED : DocumentStatus.DONE;
            document.setStatus(docStatus);
        });

        return documents;
    }

    @Override
    public void deleteDocument(String owner, Long spaceId, Long documentId) {
        var space = getSpaceOrThrow(owner, spaceId);
        var documents = databaseDocumentStorage.getDocuments(space.getId());
        var documentExists =
                documents.stream().anyMatch(document -> document.getId().equals(documentId));
        if (!documentExists) {
            throw NotFoundException.documentNotFoundException(documentId);
        }

        var mindMap = spaceStorage.readMindMap(spaceId);
        if (!mindMap.isEmpty()) {
            throw StateConflictException.mindMapAlreadyGeneratedException(documentId);
        }

        databaseDocumentStorage.deleteDocument(spaceId, documentId);
    }

    private Space getSpaceOrThrow(String username, Long spaceId) {
        var optSpace = spaceStorage.read(spaceId);
        if (optSpace.isEmpty()) {
            throw NotFoundException.spaceNotFoundException(spaceId);
        }
        var space = optSpace.get();
        return validateOwner(username, space);
    }

    private Space validateOwner(String requester, Space space) {
        if (!space.getOwner().equals(requester)) {
            throw UnauthorizedException.userNotOwnerOfSpace(requester, space.getId());
        }
        return space;
    }
}
