package ch.supsi.documentapplication.service.async.documentpipeline;

import ch.supsi.documentapplication.configuration.properties.AppProperties;
import ch.supsi.documentapplication.dataaccess.IDatabaseAiUsageStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.model.TaskType;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.async.ProcessingTasklet;
import ch.supsi.documentapplication.service.clients.OpenAITopicExtractionClient;
import ch.supsi.documentapplication.service.document.DocumentTaskProgressService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingProperties;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.retry.RetryException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Log4j2
@Component
@StepScope
public class TopicExtractionTasklet extends ProcessingTasklet {

    private static final String TOPIC_TASK_STARTED = "TOPIC_TASK_STARTED";

    private final IDatabaseDocumentStorage databaseDocumentStorage;
    private final IDatabaseTopicStorage databaseTopicStorage;
    private final OpenAITopicExtractionClient topicExtractionClient;
    private final IDatabaseAiUsageStorage aiUsageStorage;
    private final DocumentTaskProgressService documentTaskProgressService;
    private final AppProperties appProperties;
    private final VectorStore vectorStore;
    private final String embeddingModel;

    @Autowired
    public TopicExtractionTasklet(
            IDatabaseDocumentStorage databaseDocumentStorage,
            IDatabaseTopicStorage databaseTopicStorage,
            OpenAITopicExtractionClient topicExtractionClient,
            IDatabaseAiUsageStorage aiUsageStorage,
            DocumentTaskProgressService documentTaskProgressService,
            AppProperties appProperties,
            VectorStore vectorStore,
            OpenAiEmbeddingProperties openAiEmbeddingProperties) {
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.databaseTopicStorage = databaseTopicStorage;
        this.topicExtractionClient = topicExtractionClient;
        this.aiUsageStorage = aiUsageStorage;
        this.documentTaskProgressService = documentTaskProgressService;
        this.appProperties = appProperties;
        this.vectorStore = vectorStore;
        this.embeddingModel = openAiEmbeddingProperties.getModel();
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws RetryException {
        log.debug("Executing topic extraction step for space with spaceId {}", spaceId);

        Long databaseDocumentId = getDatabaseDocumentId(chunkContext);
        ExecutionContext stepExecutionContext =
                chunkContext.getStepContext().getStepExecution().getExecutionContext();
        initializeTaskIfNeeded(stepExecutionContext, databaseDocumentId);

        int processedThisStep = 0;
        int maxPagesPerStep = appProperties.getMaxChunksPerStep();
        int maxParallelism = appProperties.getMaxParallelism();

        List<Page> documentPages = databaseDocumentStorage.getPages(databaseDocumentId);
        Set<Long> documentPageIds = getDocumentPageIds(documentPages);
        List<Page> pendingPages = listPendingPages(documentPages);
        if (pendingPages.isEmpty()) {
            log.debug("Finished topic extraction step for space with spaceId {}", spaceId);
            documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC);
            return RepeatStatus.FINISHED;
        }

        AtomicInteger documentProgress = new AtomicInteger(getDocumentTopicProgress(documentPages));

        List<Topic> existingTopics = listExistingTopics(documentPages);

        try (ExecutorService executor = Executors.newFixedThreadPool(maxParallelism)) {
            while (processedThisStep < maxPagesPerStep && !pendingPages.isEmpty()) {
                int batchSize =
                        Math.min(maxParallelism, Math.min(maxPagesPerStep - processedThisStep, pendingPages.size()));
                List<Page> batch = pendingPages.subList(0, batchSize);

                extractBatchInParallel(
                        batch, existingTopics, executor, documentPageIds, databaseDocumentId, documentProgress);

                processedThisStep += batch.size();
                pendingPages = pendingPages.subList(batchSize, pendingPages.size());
                log.info("Extracted topics for {} pages in space {}", batch.size(), spaceId);
            }
        }

        documentPages = databaseDocumentStorage.getPages(databaseDocumentId);
        if (listPendingPages(documentPages).isEmpty()) {
            log.debug("Finished topic extraction step for space with spaceId {}", spaceId);
            documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC);
            return RepeatStatus.FINISHED;
        }

        return RepeatStatus.CONTINUABLE;
    }

    private void initializeTaskIfNeeded(ExecutionContext stepExecutionContext, Long databaseDocumentId) {
        if (stepExecutionContext.containsKey(TOPIC_TASK_STARTED)) {
            return;
        }

        List<Page> documentPages = databaseDocumentStorage.getPages(databaseDocumentId);
        int pendingCount = countPendingDocumentPages(documentPages);
        int maxProgress = Math.max(1, pendingCount);
        documentTaskProgressService.startTask(databaseDocumentId, TaskType.TOPIC, maxProgress);
        stepExecutionContext.put(TOPIC_TASK_STARTED, true);

        if (pendingCount == 0) {
            documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC);
        }
    }

    private int countPendingDocumentPages(List<Page> documentPages) {
        return (int) documentPages.stream()
                .filter(page -> databaseTopicStorage.getByPageId(page.getId()).isEmpty())
                .count();
    }

    private int getDocumentTopicProgress(List<Page> documentPages) {
        int pending = countPendingDocumentPages(documentPages);
        int total = documentPages.size();
        return Math.max(0, total - pending);
    }

    private Set<Long> getDocumentPageIds(List<Page> documentPages) {
        return Set.copyOf(documentPages.stream().map(Page::getId).toList());
    }

    private List<Page> listPendingPages(List<Page> documentPages) {
        return documentPages.stream()
                .filter(page -> databaseTopicStorage.getByPageId(page.getId()).isEmpty())
                .toList();
    }

    private List<Topic> listExistingTopics(List<Page> documentPages) {
        List<Topic> existingTopics = new ArrayList<>();
        for (Page page : documentPages) {
            existingTopics.addAll(databaseTopicStorage.getByPageId(page.getId()));
        }
        return existingTopics;
    }

    private void extractBatchInParallel(
            List<Page> batch,
            List<Topic> existingTopics,
            ExecutorService executor,
            Set<Long> documentPageIds,
            Long databaseDocumentId,
            AtomicInteger documentProgress)
            throws RetryException {
        List<Topic> snapshot = List.copyOf(existingTopics);

        try {
            List<Future<ClientResult<List<Topic>>>> futures = new ArrayList<>();
            for (Page page : batch) {
                futures.add(executor.submit(() -> topicExtractionClient.getResult(Pair.of(page, snapshot))));
            }

            for (int i = 0; i < batch.size(); i++) {
                Page page = batch.get(i);
                if (!documentPageIds.contains(page.getId())) {
                    throw new IllegalStateException("Refusing to extract topics for page %d outside document %d"
                            .formatted(page.getId(), databaseDocumentId));
                }

                ClientResult<List<Topic>> extractionResult = futures.get(i).get();
                List<Topic> topics = extractionResult.result();

                List<Document> documents = new ArrayList<>();
                for (Topic topic : topics) {
                    topic.setPage(page.getId());
                    topic.setPageNumber(page.getPageNumber());
                    Long topicId = databaseTopicStorage.create(topic, page.getId());
                    existingTopics.add(topic);
                    String summary = topic.getSummary();
                    if (!StringUtils.hasText(summary)) {
                        log.warn(
                                "Skipping vector store add for topic {} on page {}, empty summary",
                                topicId,
                                page.getId());
                        continue;
                    }
                    documents.add(toDocument(topicId, page.getId(), summary));
                }
                if (!documents.isEmpty()) {
                    vectorStore.add(documents);
                }

                extractionResult
                        .usages()
                        .forEach(usage -> aiUsageStorage.create("page", page.getId(), "topic_extract", usage));

                int progress = documentProgress.incrementAndGet();
                documentTaskProgressService.updateProgress(databaseDocumentId, TaskType.TOPIC, progress);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parallel topic extraction interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Parallel topic extraction failed", e.getCause());
        }
    }

    private Document toDocument(Long topicId, Long pageId, String summary) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("topicId", topicId);
        metadata.put("pageId", pageId);
        metadata.put("spaceId", spaceId);
        metadata.put("embeddingModel", embeddingModel);
        // PgVectorStore requires UUID primary keys, topics are addressed via metadata :(
        return Document.builder()
                .id(UUID.nameUUIDFromBytes(("topic:" + topicId).getBytes(StandardCharsets.UTF_8))
                        .toString())
                .text(summary)
                .metadata(metadata)
                .build();
    }
}
