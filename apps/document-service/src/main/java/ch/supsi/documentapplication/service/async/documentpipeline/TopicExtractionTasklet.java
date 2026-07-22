package ch.supsi.documentapplication.service.async.documentpipeline;

import ch.supsi.documentapplication.configuration.properties.AppProperties;
import ch.supsi.documentapplication.dataaccess.IDatabaseAiUsageStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.model.SpacePage;
import ch.supsi.documentapplication.model.TaskType;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.async.ProcessingTasklet;
import ch.supsi.documentapplication.service.clients.OpenAITopicExtractionClient;
import ch.supsi.documentapplication.service.document.DocumentTaskProgressService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
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
public class TopicExtractionTasklet extends ProcessingTasklet {

    private static final String TOPIC_TASK_STARTED = "TOPIC_TASK_STARTED";

    private final IDatabaseDocumentStorage databaseDocumentStorage;
    private final IDatabaseTopicStorage databaseTopicStorage;
    private final OpenAITopicExtractionClient topicExtractionClient;
    private final IDatabaseAiUsageStorage aiUsageStorage;
    private final DocumentTaskProgressService documentTaskProgressService;
    private final AppProperties appProperties;

    @Autowired
    public TopicExtractionTasklet(
            IDatabaseDocumentStorage databaseDocumentStorage,
            IDatabaseTopicStorage databaseTopicStorage,
            OpenAITopicExtractionClient topicExtractionClient,
            IDatabaseAiUsageStorage aiUsageStorage,
            DocumentTaskProgressService documentTaskProgressService,
            AppProperties appProperties) {
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.databaseTopicStorage = databaseTopicStorage;
        this.topicExtractionClient = topicExtractionClient;
        this.aiUsageStorage = aiUsageStorage;
        this.documentTaskProgressService = documentTaskProgressService;
        this.appProperties = appProperties;
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

        List<Page> pendingPages = listPendingPages();
        if (pendingPages.isEmpty()) {
            log.debug("Finished topic extraction step for space with spaceId {}", spaceId);
            documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC);
            return RepeatStatus.FINISHED;
        }

        Set<Long> documentPageIds = getDocumentPageIds(databaseDocumentId);
        AtomicInteger documentProgress = new AtomicInteger(getDocumentTopicProgress(databaseDocumentId));

        List<Topic> existingTopics = new ArrayList<>(databaseTopicStorage.getBySpaceId(spaceId));

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

        if (listPendingPages().isEmpty()) {
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

        int pendingCount = countPendingDocumentPages(databaseDocumentId);
        int maxProgress = Math.max(1, pendingCount);
        documentTaskProgressService.startTask(databaseDocumentId, TaskType.TOPIC, maxProgress);
        stepExecutionContext.put(TOPIC_TASK_STARTED, true);

        if (pendingCount == 0) {
            documentTaskProgressService.completeTask(databaseDocumentId, TaskType.TOPIC);
        }
    }

    private int countPendingDocumentPages(Long databaseDocumentId) {
        return (int) databaseDocumentStorage.getPages(databaseDocumentId).stream()
                .filter(page -> databaseTopicStorage.getByPageId(page.getId()).isEmpty())
                .count();
    }

    private int getDocumentTopicProgress(Long databaseDocumentId) {
        int pending = countPendingDocumentPages(databaseDocumentId);
        int total = databaseDocumentStorage.getPages(databaseDocumentId).size();
        return Math.max(0, total - pending);
    }

    private Set<Long> getDocumentPageIds(Long databaseDocumentId) {
        return new HashSet<>(databaseDocumentStorage.getPages(databaseDocumentId).stream()
                .map(Page::getId)
                .toList());
    }

    private List<Page> listPendingPages() {
        return databaseDocumentStorage.getPagesBySpaceId(spaceId).stream()
                .filter(spacePage ->
                        databaseTopicStorage.getByPageId(spacePage.getId()).isEmpty())
                .map(this::toPage)
                .toList();
    }

    private Page toPage(SpacePage spacePage) {
        return new Page(spacePage.getId(), spacePage.getTitle(), spacePage.getSummary());
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
                ClientResult<List<Topic>> extractionResult = futures.get(i).get();
                List<Topic> topics = extractionResult.result();
                existingTopics.addAll(topics);

                for (Topic topic : topics) {
                    databaseTopicStorage.create(topic, page.getId());
                }

                extractionResult
                        .usages()
                        .forEach(usage -> aiUsageStorage.create("page", page.getId(), "topic_extract", usage));

                if (documentPageIds.contains(page.getId())) {
                    int progress = documentProgress.incrementAndGet();
                    documentTaskProgressService.updateProgress(databaseDocumentId, TaskType.TOPIC, progress);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parallel topic extraction interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Parallel topic extraction failed", e.getCause());
        }
    }
}
