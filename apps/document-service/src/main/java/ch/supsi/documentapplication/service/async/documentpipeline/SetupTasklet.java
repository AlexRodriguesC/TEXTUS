package ch.supsi.documentapplication.service.async.documentpipeline;

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.documentapplication.service.async.ProcessingTasklet;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Log4j2
@Component(value = "documentSetup")
@StepScope
public class SetupTasklet extends ProcessingTasklet {

    public static final String DATABASE_DOCUMENT_ID = "DATABASE_DOCUMENT_ID";

    private final IDatabaseDocumentStorage databaseDocumentStorage;

    @Value("#{stepExecution.jobExecution.jobInstanceId}")
    private Long jobInstanceId;

    @Value("#{jobParameters['DOCUMENT_NAME']}")
    private String documentName;

    @Autowired
    public SetupTasklet(IDatabaseDocumentStorage databaseDocumentStorage) {
        this.databaseDocumentStorage = databaseDocumentStorage;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) {

        var jobExecutionContext = chunkContext.getStepContext().getJobExecutionContext();
        if (jobExecutionContext.containsKey(DATABASE_DOCUMENT_ID)) {
            log.debug("Setup already completed for job instance {}, skipping", jobInstanceId);
            return RepeatStatus.FINISHED;
        }

        var existingDocumentId = databaseDocumentStorage.findIdByJobId(jobInstanceId);
        if (existingDocumentId.isPresent()) {
            log.debug(
                    "Document record already exists for job instance {}, reusing id {}",
                    jobInstanceId,
                    existingDocumentId.get());
            chunkContext
                    .getStepContext()
                    .getStepExecution()
                    .getExecutionContext()
                    .put(DATABASE_DOCUMENT_ID, existingDocumentId.get());
            return RepeatStatus.FINISHED;
        }

        Long databaseDocId = databaseDocumentStorage.create(spaceId, jobInstanceId, documentName);
        chunkContext.getStepContext().getStepExecution().getExecutionContext().put(DATABASE_DOCUMENT_ID, databaseDocId);

        return RepeatStatus.FINISHED;
    }
}
