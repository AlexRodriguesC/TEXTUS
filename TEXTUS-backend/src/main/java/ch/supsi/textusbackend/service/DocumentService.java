package ch.supsi.textusbackend.service;

import ch.supsi.textusbackend.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.textusbackend.dataaccess.IFileSystemDocumentStorage;
import ch.supsi.textusbackend.exception.DocumentDomainException;
import ch.supsi.textusbackend.model.Document;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Service
public class DocumentService implements IDocumentService {

    private final IFileSystemDocumentStorage documentStorage;
    private final IDatabaseDocumentStorage databaseDocumentStorage;
    private final JobOperator jobOperator;
    private final Job pipelineJob;

    @Autowired
    public DocumentService(
            IFileSystemDocumentStorage documentStorage,
            IDatabaseDocumentStorage databaseDocumentStorage,
            JobOperator asyncJobOperator,
            Job pipelineJob) {
        this.documentStorage = documentStorage;
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.jobOperator = asyncJobOperator;
        this.pipelineJob = pipelineJob;
    }

    @Override
    @Transactional // todo: this doesn't really work
    public void parseDocument(MultipartFile documentMultipart) {
        byte[] documentBytes;
        try {
            documentBytes = documentMultipart.getBytes();
        } catch (Exception e) {
            String message = String.format("Failed to read bytes for file %s", documentMultipart.getOriginalFilename());
            log.error(message, e);
            throw new DocumentDomainException("Failed to get file bytes", e);
        }

        Document document = Document.create(documentBytes);

        documentStorage.create(document);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("DOCUMENT_ID", document.getDocumentId().toString())
                .addLong("TRIGGERED_AT", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobOperator.start(pipelineJob, jobParameters);
        } catch (Exception e) {
            String message = String.format("Failed to start job %s", pipelineJob.getName());
            log.error(message, e);
            throw new DocumentDomainException("Failed to start job", e);
        }
    }
}
