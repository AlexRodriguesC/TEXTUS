package ch.supsi.documentapplication.service.documentpipeline;

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.documentapplication.service.ProcessingTasklet;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component(value = "documentSetup")
@StepScope
public class SetupTasklet extends ProcessingTasklet {

    public static final String DATABASE_DOCUMENT_ID = "DATABASE_DOCUMENT_ID";

    private final IDatabaseDocumentStorage databaseDocumentStorage;

    @Autowired
    public SetupTasklet(IDatabaseDocumentStorage databaseDocumentStorage) {
        this.databaseDocumentStorage = databaseDocumentStorage;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) {

        var jobId = chunkContext.getStepContext().getJobInstanceId();
        Long databaseDocId = databaseDocumentStorage.create(spaceId, jobId);
        chunkContext.getStepContext().getStepExecution().getExecutionContext().put(DATABASE_DOCUMENT_ID, databaseDocId);

        return RepeatStatus.FINISHED;
    }
}
