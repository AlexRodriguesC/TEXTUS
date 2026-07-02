package ch.supsi.documentapplication.service.mindmappipeline;

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage;
import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.model.Topic;
import ch.supsi.documentapplication.service.ProcessingTasklet;
import ch.supsi.documentapplication.service.clients.OpenAITopicExtractionClient;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@StepScope
public class TopicExtractionTasklet extends ProcessingTasklet {

    private final IDatabaseDocumentStorage databaseDocumentStorage;
    private final IDatabaseTopicStorage databaseTopicStorage;
    private final OpenAITopicExtractionClient topicExtractionClient;

    @Autowired
    public TopicExtractionTasklet(
            IDatabaseDocumentStorage databaseDocumentStorage,
            IDatabaseTopicStorage databaseTopicStorage,
            OpenAITopicExtractionClient topicExtractionClient) {
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.databaseTopicStorage = databaseTopicStorage;
        this.topicExtractionClient = topicExtractionClient;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws RetryException {
        log.debug("Executing topic extraction step for space with spaceId {}", spaceId);

        var documents = databaseDocumentStorage.getDocuments(spaceId);
        List<Topic> existingTopics = new ArrayList<>();

        for (var document : documents) {
            List<Page> documentPages = databaseDocumentStorage.getPages(document.getId());
            var documentTopics = databaseTopicStorage.getByDocumentId(document.getId());

            if (!documentTopics.isEmpty()) {
                existingTopics.addAll(documentTopics);
                continue;
            }

            for (Page page : documentPages) {

                List<Topic> topics = topicExtractionClient.getResult(Pair.of(page, existingTopics));
                existingTopics.addAll(topics);

                for (Topic topic : topics) {
                    databaseTopicStorage.create(topic, page.getId());
                }
            }
        }

        return RepeatStatus.FINISHED;
    }
}
