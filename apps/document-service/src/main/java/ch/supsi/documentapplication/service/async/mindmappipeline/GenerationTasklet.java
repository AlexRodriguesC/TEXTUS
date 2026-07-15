package ch.supsi.documentapplication.service.async.mindmappipeline;

import ch.supsi.documentapplication.dataaccess.DatabaseDocumentStorage;
import ch.supsi.documentapplication.dataaccess.DatabaseTopicStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.model.*;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.async.ProcessingTasklet;
import ch.supsi.documentapplication.service.clients.OpenAIMindMapClient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.ExitStatus;
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
public class GenerationTasklet extends ProcessingTasklet {

    private final DatabaseTopicStorage databaseTopicStorage;
    private final DatabaseDocumentStorage databaseDocumentStorage;
    private final IDatabaseSpaceStorage databaseSpaceStorage;
    private final OpenAIMindMapClient mindMapClient;

    @Autowired
    public GenerationTasklet(
            IDatabaseSpaceStorage databaseSpaceStorage,
            OpenAIMindMapClient mindMapClient,
            DatabaseDocumentStorage databaseDocumentStorage,
            DatabaseTopicStorage databaseTopicStorage) {
        this.databaseSpaceStorage = databaseSpaceStorage;
        this.mindMapClient = mindMapClient;
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.databaseTopicStorage = databaseTopicStorage;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws RetryException {

        Long mapId =
                (Long) chunkContext.getStepContext().getJobExecutionContext().get("MAP_ID");

        var documents = databaseDocumentStorage.getDocuments(spaceId);
        List<Topic> topics = new ArrayList<>();
        for (Document document : documents) {
            var pages = databaseDocumentStorage.getPages(document.getId());
            var docTopics = new ArrayList<Topic>();
            for (Page page : pages) {
                var pageTopics = databaseTopicStorage.getByPageId(page.getId());
                docTopics.addAll(pageTopics);
            }
            topics.addAll(docTopics);
        }

        topics = topics.stream().distinct().collect(Collectors.toList());

        if (topics.isEmpty()) {
            contribution.setExitStatus(ExitStatus.FAILED);
            return RepeatStatus.FINISHED;
        }

        MindMap mindMap = mindMapClient.getResult(topics);

        databaseSpaceStorage.createMindMapNodes(spaceId, mapId, mindMap);

        return RepeatStatus.FINISHED;
    }
}
