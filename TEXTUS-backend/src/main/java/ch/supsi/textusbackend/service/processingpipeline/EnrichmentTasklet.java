package ch.supsi.textusbackend.service.processingpipeline;

import ch.supsi.textusbackend.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.textusbackend.dataaccess.IFileSystemDocumentStorage;
import ch.supsi.textusbackend.model.Chunk;
import ch.supsi.textusbackend.properties.AppProperties;
import ch.supsi.textusbackend.service.clients.enrichment.EnrichmentClient;
import java.util.List;
import java.util.Locale;
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
@Component
@StepScope
public class EnrichmentTasklet extends ProcessingTasklet {

    private final AppProperties appProperties;
    private final IDatabaseDocumentStorage databaseBookStorage;
    private final IFileSystemDocumentStorage fileSystemBookStorage;
    private final EnrichmentClient enrichmentClient;

    @Autowired
    public EnrichmentTasklet(
            IDatabaseDocumentStorage databaseBookStorage,
            IFileSystemDocumentStorage fileSystemBookStorage,
            EnrichmentClient enrichmentClient,
            AppProperties appProperties) {
        this.databaseBookStorage = databaseBookStorage;
        this.fileSystemBookStorage = fileSystemBookStorage;
        this.enrichmentClient = enrichmentClient;
        this.appProperties = appProperties;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext) {
        log.debug("Executing enrichment step for book with bookId {}", documentId);

        final int MAX_CHUNKS_PER_EXECUTION = 5;

        var stepExecutionContext =
                chunkContext.getStepContext().getStepExecution().getExecutionContext();

        String previousSummary = stepExecutionContext.containsKey("previousSummary")
                ? stepExecutionContext.getString("previousSummary")
                : null;

        List<String> rollingKeys = stepExecutionContext.containsKey("rollingKeys")
                ? new java.util.ArrayList<>((List<String>) stepExecutionContext.get("rollingKeys"))
                : new java.util.ArrayList<>();

        int totalProcessed =
                stepExecutionContext.containsKey("totalProcessed") ? stepExecutionContext.getInt("totalProcessed") : 0;

        int processed = 0;

        while (processed < MAX_CHUNKS_PER_EXECUTION) {
            String chunkText = fileSystemBookStorage.readNextChunk(documentId);

            if (chunkText == null || chunkText.isBlank()) {
                log.info("No more chunks to enrich for book {}", documentId);
                return RepeatStatus.FINISHED;
            }

            Chunk enrichedChunk = enrichmentClient.enrich(previousSummary, chunkText, List.copyOf(rollingKeys));

            if (enrichedChunk == null) {
                log.warn("Failed to enrich chunk for book {} and page {}", documentId, totalProcessed);
                continue;
            }

            enrichedChunk.setPage(totalProcessed + 1);
            enrichedChunk.setKey(enrichedChunk.getKey().toLowerCase(Locale.ROOT));

            databaseBookStorage.create(enrichedChunk, documentId);

            fileSystemBookStorage.deleteChunk(documentId, String.valueOf(enrichedChunk.getPage()));

            previousSummary = enrichedChunk.getSummary();

            if (enrichedChunk.getKey() != null && !enrichedChunk.getKey().isBlank()) {
                rollingKeys.add(enrichedChunk.getKey().toLowerCase(Locale.ROOT));
            }

            if (rollingKeys.size() > appProperties.getMaxRollingKeySize()) {
                rollingKeys = new java.util.ArrayList<>(rollingKeys.subList(
                        rollingKeys.size() - appProperties.getMaxRollingKeySize(), rollingKeys.size()));
            }

            processed++;
            totalProcessed++;
            log.info("Processed enriched chunk for book {} page {}", documentId, enrichedChunk.getPage());
        }

        stepExecutionContext.putString("previousSummary", previousSummary == null ? "" : previousSummary);
        stepExecutionContext.putInt("totalProcessed", totalProcessed);
        stepExecutionContext.put("rollingKeys", rollingKeys);

        return RepeatStatus.CONTINUABLE;
    }
}
