package ch.supsi.documentapplication.service.documentpipeline;

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentStorage;
import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.model.Topic;
import ch.supsi.documentapplication.properties.AppProperties;
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
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
public class TopicExtractionTasklet extends ProcessingTasklet {

    private final IDatabaseDocumentStorage databaseDocumentStorage;
    private final OpenAITopicExtractionClient topicExtractionClient;
    private final AppProperties appProperties;

    @Autowired
    public TopicExtractionTasklet(
            IDatabaseDocumentStorage databaseDocumentStorage,
            OpenAITopicExtractionClient topicExtractionClient,
            AppProperties appProperties) {
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.topicExtractionClient = topicExtractionClient;
        this.appProperties = appProperties;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws Exception {
        log.debug("Executing topic extraction step for document with documentId {}", databaseDocumentId);

        List<Page> documentPages = databaseDocumentStorage.getPages(databaseDocumentId);
        List<Topic> existingTopics = new ArrayList<>();

        for (Page page : documentPages) {

            List<Topic> topics = topicExtractionClient.getResult(Pair.of(page, existingTopics));
            existingTopics.addAll(topics);

            for (Topic topic : topics) {
                databaseDocumentStorage.create(topic, page.getId());
            }
        }

        return RepeatStatus.FINISHED;
    }
}
