package ch.supsi.documentapplication.service.document;

import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage;
import ch.supsi.documentapplication.exception.DocumentDomainException;
import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.model.Document;
import ch.supsi.documentapplication.model.Space;
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

    private final IFileSystemDocumentStorage documentStorage;
    private final IDatabaseSpaceStorage spaceStorage;
    private final JobOperator jobOperator;
    private final Job pipelineJob;

    @Autowired
    public DocumentService(
            IFileSystemDocumentStorage documentStorage,
            IDatabaseSpaceStorage spaceStorage,
            JobOperator asyncJobOperator,
            Job pipelineJob) {
        this.documentStorage = documentStorage;
        this.spaceStorage = spaceStorage;
        this.jobOperator = asyncJobOperator;
        this.pipelineJob = pipelineJob;
    }

    @Override
    public void createDocument(MultipartFile documentMultipart, Long spaceId) {
        byte[] documentBytes;
        try {
            documentBytes = documentMultipart.getBytes();
        } catch (Exception e) {
            String message = String.format("Failed to read bytes for file %s", documentMultipart.getOriginalFilename());
            log.error(message, e);
            throw new DocumentDomainException("Failed to get file bytes", e);
        }

        Space space = spaceStorage.read(spaceId);

        if (space == null) {
            throw NotFoundException.spaceNotFoundException(spaceId);
        }

        Document document = Document.create(documentBytes);

        documentStorage.create(document);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("DOCUMENT_ID", document.getDocumentId().toString())
                .addString("SPACE_ID", spaceId.toString())
                .addLong("TRIGGERED_AT", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobOperator.start(pipelineJob, jobParameters);
        } catch (Exception e) {
            String message = String.format("Failed to start job %s", pipelineJob.getName());
            log.error(message, e);
            throw DocumentDomainException.jobFailedException(e);
        }
    }
}
