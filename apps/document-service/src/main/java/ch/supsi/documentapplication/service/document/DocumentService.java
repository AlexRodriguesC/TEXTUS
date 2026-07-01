package ch.supsi.documentapplication.service.document;

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage;
import ch.supsi.documentapplication.exception.*;
import ch.supsi.documentapplication.model.Document;
import ch.supsi.documentapplication.model.FsDocument;
import ch.supsi.documentapplication.model.Space;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Service
public class DocumentService implements IDocumentService {

    private final IFileSystemDocumentStorage documentFsStorage;
    private final IDatabaseDocumentStorage databaseDocumentStorage;
    private final IDatabaseSpaceStorage spaceStorage;
    private final JobOperator jobOperator;
    private final Job pipelineJob;

    @Autowired
    public DocumentService(
            IFileSystemDocumentStorage documentFsStorage,
            IDatabaseDocumentStorage databaseDocumentStorage,
            IDatabaseSpaceStorage spaceStorage,
            JobOperator asyncJobOperator,
            Job pipelineJob) {
        this.documentFsStorage = documentFsStorage;
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.spaceStorage = spaceStorage;
        this.jobOperator = asyncJobOperator;
        this.pipelineJob = pipelineJob;
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

        Optional<Space> optSpace = spaceStorage.read(spaceId);

        if (optSpace.isEmpty()) {
            throw NotFoundException.spaceNotFoundException(spaceId);
        }

        var space = optSpace.get();

        if (!space.getOwner().equals(owner)) {
            throw UnauthorizedException.userNotOwnerOfSpace(owner, space.getId());
        }

        FsDocument fsDocument = FsDocument.create(documentBytes);

        Long databaseDocId = databaseDocumentStorage.create(spaceId);
        documentFsStorage.create(fsDocument);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("DOCUMENT_ID", fsDocument.getDocumentId().toString())
                .addLong("DATABASE_DOCUMENT_ID", databaseDocId)
                .addLong("SPACE_ID", spaceId)
                .addLong("TRIGGERED_AT", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobOperator.start(pipelineJob, jobParameters);
        } catch (Exception e) {
            String message = String.format("Failed to start job %s", pipelineJob.getName());
            log.error(message, e);
            throw new JobFailedToStartException(message);
        }
    }

    @Override
    public List<Document> getDocuments(String owner, Long spaceId) {
        var space = getSpaceOrThrow(owner, spaceId);
        return databaseDocumentStorage.getDocuments(space.getId());
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
