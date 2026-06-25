package ch.supsi.documentapplication.service.documentpipeline;

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage;
import ch.supsi.documentapplication.model.Chunk;
import ch.supsi.documentapplication.properties.AppProperties;
import ch.supsi.documentapplication.service.clients.enrichment.EnrichmentClient;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Log4j2
@Component
@StepScope
public class EnrichmentTasklet extends ProcessingTasklet {

    private static final String PREVIOUS_SUMMARY = "PREVIOUS_SUMMARY";
    private static final String ROLLING_KEYS = "ROLLING_KEYS";
    private static final String TOTAL_PROCESSED = "TOTAL_PROCESSED";
    private static final String CHUNK_IDS = "CHUNK_IDS";

    private final IDatabaseDocumentStorage databaseDocumentStorage;
    private final IFileSystemDocumentStorage fileSystemDocumentStorage;
    private final EnrichmentClient enrichmentClient;
    private final AppProperties appProperties;

    @Autowired
    public EnrichmentTasklet(
            IDatabaseDocumentStorage databaseDocumentStorage,
            IFileSystemDocumentStorage fileSystemDocumentStorage,
            EnrichmentClient enrichmentClient,
            AppProperties appProperties) {
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.fileSystemDocumentStorage = fileSystemDocumentStorage;
        this.enrichmentClient = enrichmentClient;
        this.appProperties = appProperties;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) {
        log.debug("Executing enrichment step for document with documentId {}", documentId);

        var stepExecutionContext =
                chunkContext.getStepContext().getStepExecution().getExecutionContext();

        var previousSummary = getValue(stepExecutionContext, PREVIOUS_SUMMARY, null, String.class);
        var rollingKeys = getListValue(stepExecutionContext, ROLLING_KEYS, new ArrayList<>(), String.class);
        var totalProcessed = getValue(stepExecutionContext, TOTAL_PROCESSED, 0, Integer.class);

        int processed = 0;
        var chunkIds = getListValue(stepExecutionContext, CHUNK_IDS, new ArrayList<>(), Long.class);

        while (processed < appProperties.getMaxChunksPerStep()) {
            String chunkText = fileSystemDocumentStorage.readNextChunk(getDocumentIdAsUUID());

            if (chunkText == null || chunkText.isBlank()) {
                log.info("No more chunks to enrich for document with documentId: {}", documentId);
                chunkContext
                        .getStepContext()
                        .getStepExecution()
                        .getJobExecution()
                        .getExecutionContext()
                        .put(CHUNK_IDS, chunkIds);
                return RepeatStatus.FINISHED;
            }

            Chunk enrichedChunk = enrichmentClient.enrich(previousSummary, chunkText, List.copyOf(rollingKeys));

            if (enrichedChunk == null) {
                log.warn(
                        "Failed to enrich chunk for document with documentId: {} and page: {}",
                        documentId,
                        totalProcessed);
                continue;
            }

            enrichedChunk.setKey(enrichedChunk.getKey().toLowerCase(Locale.ROOT));

            Long chunkId = databaseDocumentStorage.create(enrichedChunk, getSpaceIdAsLong());
            chunkIds.add(chunkId);

            fileSystemDocumentStorage.deleteChunk(getDocumentIdAsUUID(), String.valueOf(totalProcessed + 1));

            previousSummary = enrichedChunk.getSummary();

            if (enrichedChunk.getKey() != null && !enrichedChunk.getKey().isBlank()) {
                rollingKeys.add(enrichedChunk.getKey().toLowerCase(Locale.ROOT));
            }

            if (rollingKeys.size() > appProperties.getMaxRollingKeySize()) {
                rollingKeys = new ArrayList<>(rollingKeys.subList(
                        rollingKeys.size() - appProperties.getMaxRollingKeySize(), rollingKeys.size()));
            }

            processed++;
            totalProcessed++;
            log.info(
                    "Processed enriched chunk for document with documentId: {}, page: {}",
                    documentId,
                    totalProcessed + 1);
        }

        stepExecutionContext.putString(PREVIOUS_SUMMARY, previousSummary == null ? "" : previousSummary);
        stepExecutionContext.putInt(TOTAL_PROCESSED, totalProcessed);
        stepExecutionContext.put(ROLLING_KEYS, rollingKeys);
        stepExecutionContext.put(CHUNK_IDS, chunkIds);

        return RepeatStatus.CONTINUABLE;
    }
}
