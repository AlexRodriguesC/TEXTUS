package ch.supsi.documentapplication.service.documentpipeline;

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage;
import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.properties.AppProperties;
import ch.supsi.documentapplication.service.ProcessingTasklet;
import ch.supsi.documentapplication.service.clients.OpenAISummarizationClient;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
public class SummarizationTasklet extends ProcessingTasklet {

    private static final String TOTAL_PROCESSED = "TOTAL_PROCESSED";

    private final IDatabaseDocumentStorage databaseDocumentStorage;
    private final IFileSystemDocumentStorage fileSystemDocumentStorage;
    private final OpenAISummarizationClient summarisationClient;
    private final AppProperties appProperties;

    @Autowired
    public SummarizationTasklet(
            IDatabaseDocumentStorage databaseDocumentStorage,
            IFileSystemDocumentStorage fileSystemDocumentStorage,
            OpenAISummarizationClient summarisationClient,
            AppProperties appProperties) {
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.fileSystemDocumentStorage = fileSystemDocumentStorage;
        this.summarisationClient = summarisationClient;
        this.appProperties = appProperties;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) {
        log.debug("Executing summarization step for document with documentId {}", databaseDocumentId);

        var stepExecutionContext =
                chunkContext.getStepContext().getStepExecution().getExecutionContext();

        var totalProcessed = getValue(stepExecutionContext, TOTAL_PROCESSED, 0, Integer.class);

        int processed = 0;

        while (processed < appProperties.getMaxChunksPerStep()) {
            String pageText = fileSystemDocumentStorage.readNextPage(getDocumentIdAsUUID());

            if (pageText == null || pageText.isBlank()) {
                log.info("No more pages to summarize for document with documentId: {}", databaseDocumentId);
                return RepeatStatus.FINISHED;
            }

            Page page = summarisationClient.getResult(pageText);
            databaseDocumentStorage.create(page, spaceId, databaseDocumentId);

            fileSystemDocumentStorage.deleteChunk(getDocumentIdAsUUID(), String.valueOf(totalProcessed + 1));

            processed++;
            totalProcessed++;
            log.info(
                    "Processed page for document with documentId: {}, page: {}",
                    databaseDocumentId,
                    totalProcessed + 1);
        }

        stepExecutionContext.putInt(TOTAL_PROCESSED, totalProcessed);

        return RepeatStatus.CONTINUABLE;
    }
}
