package ch.supsi.documentapplication.service.async.documentpipeline;

import ch.supsi.documentapplication.configuration.properties.AppProperties;
import ch.supsi.documentapplication.dataaccess.IDatabaseAiUsageStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage;
import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.model.PageChunk;
import ch.supsi.documentapplication.model.PageSummarization;
import ch.supsi.documentapplication.model.TaskType;
import ch.supsi.documentapplication.service.async.ProcessingTasklet;
import ch.supsi.documentapplication.service.clients.OpenAISummarizationClient;
import ch.supsi.documentapplication.service.document.DocumentTaskProgressService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.retry.RetryException;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@StepScope
public class SummarizationTasklet extends ProcessingTasklet {

    private static final String TOTAL_PROCESSED = "TOTAL_PROCESSED";
    private static final String LAST_PROCESSED_CHUNK_PAGE = "LAST_PROCESSED_CHUNK_PAGE";
    private static final String SUMMARIZATION_TASK_STARTED = "SUMMARIZATION_TASK_STARTED";
    private static final String INITIAL_PAGE_COUNT = "INITIAL_PAGE_COUNT";

    private final IDatabaseDocumentStorage databaseDocumentStorage;
    private final IFileSystemDocumentStorage fileSystemDocumentStorage;
    private final OpenAISummarizationClient summarisationClient;
    private final IDatabaseAiUsageStorage aiUsageStorage;
    private final DocumentTaskProgressService documentTaskProgressService;
    private final AppProperties appProperties;

    @Autowired
    public SummarizationTasklet(
            IDatabaseDocumentStorage databaseDocumentStorage,
            IFileSystemDocumentStorage fileSystemDocumentStorage,
            OpenAISummarizationClient summarisationClient,
            IDatabaseAiUsageStorage aiUsageStorage,
            DocumentTaskProgressService documentTaskProgressService,
            AppProperties appProperties) {
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.fileSystemDocumentStorage = fileSystemDocumentStorage;
        this.summarisationClient = summarisationClient;
        this.aiUsageStorage = aiUsageStorage;
        this.documentTaskProgressService = documentTaskProgressService;
        this.appProperties = appProperties;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws RetryException {
        ExecutionContext stepExecutionContext =
                chunkContext.getStepContext().getStepExecution().getExecutionContext();

        int totalProcessed = getValue(stepExecutionContext, TOTAL_PROCESSED, 0, Integer.class);
        int lastProcessedChunkPage = getValue(stepExecutionContext, LAST_PROCESSED_CHUNK_PAGE, 0, Integer.class);
        Long databaseDocumentId =
                (Long) chunkContext.getStepContext().getJobExecutionContext().get("DATABASE_DOCUMENT_ID");
        UUID fsDocumentId = getDocumentIdAsUUID();

        log.debug("Executing summarization step for document with documentId {}", databaseDocumentId);

        initializeTaskIfNeeded(stepExecutionContext, databaseDocumentId, fsDocumentId);

        int processedThisStep = 0;
        int maxChunksPerStep = appProperties.getMaxChunksPerStep();
        int maxParallelism = appProperties.getMaxParallelism();

        while (processedThisStep < maxChunksPerStep) {
            if (fileSystemDocumentStorage.readNextChunk(fsDocumentId).isEmpty()) {
                log.info("No more pages to summarize for document with documentId: {}", databaseDocumentId);
                documentTaskProgressService.completeTask(databaseDocumentId, TaskType.SUMMARIZATION);
                return RepeatStatus.FINISHED;
            }

            if (recoverNextChunkIfNeeded(
                    stepExecutionContext, fsDocumentId, databaseDocumentId, totalProcessed, lastProcessedChunkPage)) {
                totalProcessed = stepExecutionContext.getInt(TOTAL_PROCESSED);
                lastProcessedChunkPage = stepExecutionContext.getInt(LAST_PROCESSED_CHUNK_PAGE);
                updateSummarizationProgress(stepExecutionContext, databaseDocumentId, totalProcessed);
                processedThisStep++;
                continue;
            }

            int batchLimit = maxChunksPerStep - processedThisStep;
            int minPageExclusive = lastProcessedChunkPage;
            List<PageChunk> batch = fileSystemDocumentStorage.listChunks(fsDocumentId, batchLimit).stream()
                    .filter(chunk -> chunk.pageNumber() > minPageExclusive)
                    .toList();

            if (batch.isEmpty()) {
                break;
            }

            lastProcessedChunkPage = summarizeBatchInParallel(
                    batch, databaseDocumentId, fsDocumentId, maxParallelism, lastProcessedChunkPage);
            totalProcessed += batch.size();
            stepExecutionContext.putInt(TOTAL_PROCESSED, totalProcessed);
            stepExecutionContext.putInt(LAST_PROCESSED_CHUNK_PAGE, lastProcessedChunkPage);
            updateSummarizationProgress(stepExecutionContext, databaseDocumentId, totalProcessed);

            processedThisStep += batch.size();
            log.info(
                    "Summarized {} pages in parallel for document with documentId: {}",
                    batch.size(),
                    databaseDocumentId);
        }

        return RepeatStatus.CONTINUABLE;
    }

    private void initializeTaskIfNeeded(
            ExecutionContext stepExecutionContext, Long databaseDocumentId, UUID fsDocumentId) {
        if (stepExecutionContext.containsKey(SUMMARIZATION_TASK_STARTED)) {
            return;
        }

        int existingPages = databaseDocumentStorage.getPages(databaseDocumentId).size();
        int remainingChunks = fileSystemDocumentStorage
                .listChunks(fsDocumentId, Integer.MAX_VALUE)
                .size();
        int maxProgress = Math.max(1, existingPages + remainingChunks);

        documentTaskProgressService.startTask(databaseDocumentId, TaskType.SUMMARIZATION, maxProgress);
        stepExecutionContext.putInt(INITIAL_PAGE_COUNT, existingPages);
        stepExecutionContext.put(SUMMARIZATION_TASK_STARTED, true);
    }

    private void updateSummarizationProgress(
            ExecutionContext stepExecutionContext, Long databaseDocumentId, int totalProcessed) {
        int initialPages = stepExecutionContext.getInt(INITIAL_PAGE_COUNT);
        documentTaskProgressService.updateProgress(
                databaseDocumentId, TaskType.SUMMARIZATION, initialPages + totalProcessed);
    }

    private boolean recoverNextChunkIfNeeded(
            ExecutionContext stepExecutionContext,
            UUID fsDocumentId,
            Long databaseDocumentId,
            int totalProcessed,
            int lastProcessedChunkPage) {
        PageChunk chunk = fileSystemDocumentStorage
                .readNextChunk(fsDocumentId)
                .orElseThrow(() -> new IllegalStateException("Expected pending chunk during summarization recovery"));

        int chunkPage = chunk.pageNumber();
        List<Page> existingPages = databaseDocumentStorage.getPages(databaseDocumentId);

        if (existingPages.size() > totalProcessed) {
            log.info(
                    "Recovering summarization checkpoint for document {}, skipping already persisted page chunk {}",
                    databaseDocumentId,
                    chunkPage);
            fileSystemDocumentStorage.deleteChunk(fsDocumentId, String.valueOf(chunkPage));
            stepExecutionContext.putInt(TOTAL_PROCESSED, existingPages.size());
            stepExecutionContext.putInt(LAST_PROCESSED_CHUNK_PAGE, chunkPage);
            return true;
        }

        if (chunkPage <= lastProcessedChunkPage) {
            log.info(
                    "Removing orphan chunk {} for document {} during summarization recovery",
                    chunkPage,
                    databaseDocumentId);
            fileSystemDocumentStorage.deleteChunk(fsDocumentId, String.valueOf(chunkPage));
            return true;
        }

        return false;
    }

    private int summarizeBatchInParallel(
            List<PageChunk> batch,
            Long databaseDocumentId,
            UUID fsDocumentId,
            int maxParallelism,
            int lastProcessedChunkPage)
            throws RetryException {
        int poolSize = Math.min(maxParallelism, batch.size());

        try (ExecutorService executor = Executors.newFixedThreadPool(poolSize)) {
            List<Future<Integer>> futures = new ArrayList<>();

            for (PageChunk chunk : batch) {
                futures.add(executor.submit(() -> summarizeChunk(chunk, databaseDocumentId, fsDocumentId)));
            }

            int maxPage = lastProcessedChunkPage;
            for (Future<Integer> future : futures) {
                maxPage = Math.max(maxPage, future.get());
            }

            return maxPage;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parallel summarization interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Parallel summarization failed", e.getCause());
        }
    }

    private int summarizeChunk(PageChunk chunk, Long databaseDocumentId, UUID fsDocumentId) throws RetryException {
        if (chunk.content().isBlank()) {
            log.info(
                    "Skipping empty page for document with documentId: {}, page: {}",
                    databaseDocumentId,
                    chunk.pageNumber());
            fileSystemDocumentStorage.deleteChunk(fsDocumentId, String.valueOf(chunk.pageNumber()));
            return chunk.pageNumber();
        }
        var result = summarisationClient.getResult(chunk.content());
        PageSummarization summarization = result.result();
        if (summarization.isContentPage()) {
            Page page = new Page(null, summarization.getTitle(), summarization.getSummary());
            Long pageId = databaseDocumentStorage.create(page, spaceId, databaseDocumentId);
            result.usages().forEach(usage -> aiUsageStorage.create("page", pageId, "summarize", usage));
            log.info(
                    "Processed content page for document with documentId: {}, page: {}",
                    databaseDocumentId,
                    chunk.pageNumber());
        } else {
            result.usages().forEach(usage -> aiUsageStorage.create("document", databaseDocumentId, "summarize", usage));
            log.info(
                    "Skipped non-content page for document with documentId: {}, page: {}",
                    databaseDocumentId,
                    chunk.pageNumber());
        }
        fileSystemDocumentStorage.deleteChunk(fsDocumentId, String.valueOf(chunk.pageNumber()));
        return chunk.pageNumber();
    }
}
