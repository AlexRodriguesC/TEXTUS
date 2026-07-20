package ch.supsi.documentapplication.service.async.documentpipeline;

import ch.supsi.documentapplication.configuration.properties.AppProperties;
import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage;
import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.async.ProcessingTasklet;
import ch.supsi.documentapplication.service.clients.OpenAITopicExtractionClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.retry.RetryException;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@StepScope
public class TopicExtractionTasklet extends ProcessingTasklet {

    private final IDatabaseDocumentStorage databaseDocumentStorage;
    private final IDatabaseTopicStorage databaseTopicStorage;
    private final OpenAITopicExtractionClient topicExtractionClient;
    private final AppProperties appProperties;

    @Autowired
    public TopicExtractionTasklet(
            IDatabaseDocumentStorage databaseDocumentStorage,
            IDatabaseTopicStorage databaseTopicStorage,
            OpenAITopicExtractionClient topicExtractionClient,
            AppProperties appProperties) {
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.databaseTopicStorage = databaseTopicStorage;
        this.topicExtractionClient = topicExtractionClient;
        this.appProperties = appProperties;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws RetryException {
        log.debug("Executing topic extraction step for space with spaceId {}", spaceId);

        var documents = databaseDocumentStorage.getDocuments(spaceId);
        List<Topic> existingTopics = new ArrayList<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(appProperties.getMaxParallelism()); ) {
            for (var document : documents) {
                List<Page> documentPages = databaseDocumentStorage.getPages(document.getId());
                var documentTopics = databaseTopicStorage.getByDocumentId(document.getId());
                existingTopics.addAll(documentTopics);

                for (List<Page> batch : partition(documentPages, appProperties.getMaxParallelism())) {
                    List<Topic> snapshot = List.copyOf(existingTopics);

                    List<CompletableFuture<List<Topic>>> futures = batch.stream()
                            .filter(page -> databaseTopicStorage
                                    .getByPageId(page.getId())
                                    .isEmpty())
                            .map(page -> CompletableFuture.supplyAsync(
                                    () -> topicExtractionClient.getResult(Pair.of(page, snapshot)), executor))
                            .toList();

                    List<Page> pagesToProcess = batch.stream()
                            .filter(page -> databaseTopicStorage
                                    .getByPageId(page.getId())
                                    .isEmpty())
                            .toList();

                    for (int i = 0; i < pagesToProcess.size(); i++) {
                        Page page = pagesToProcess.get(i);
                        List<Topic> topics = futures.get(i).join();
                        existingTopics.addAll(topics);

                        for (Topic topic : topics) {
                            databaseTopicStorage.create(topic, page.getId());
                        }
                    }
                }
            }
        }

        log.debug("Finished topic extraction step for space with spaceId {}", spaceId);
        return RepeatStatus.FINISHED;
    }

    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }
}
