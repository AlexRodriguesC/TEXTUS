package ch.supsi.documentapplication.service.mindmappipeline;

import ch.supsi.documentapplication.dataaccess.DatabaseDocumentStorage;
import ch.supsi.documentapplication.dataaccess.DatabaseTopicStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.model.*;
import ch.supsi.documentapplication.service.clients.mindmap.MindMapClient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@StepScope
public class GenerationTasklet implements Tasklet {

    private final DatabaseTopicStorage databaseTopicStorage;

    @Value("#{jobParameters['SPACE_ID']}")
    protected Long spaceId;

    private final DatabaseDocumentStorage databaseDocumentStorage;

    private final IDatabaseSpaceStorage databaseSpaceStorage;

    private final MindMapClient mindMapClient;

    @Autowired
    public GenerationTasklet(
            IDatabaseSpaceStorage databaseSpaceStorage,
            MindMapClient mindMapClient,
            DatabaseDocumentStorage databaseDocumentStorage,
            DatabaseTopicStorage databaseTopicStorage) {
        this.databaseSpaceStorage = databaseSpaceStorage;
        this.mindMapClient = mindMapClient;
        this.databaseDocumentStorage = databaseDocumentStorage;
        this.databaseTopicStorage = databaseTopicStorage;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws Exception {

        var optSpace = databaseSpaceStorage.read(spaceId);
        if (optSpace.isEmpty()) {
            var message = String.format("No space found with id %s", spaceId);
            log.error(message);
            return RepeatStatus.FINISHED;
        }

        var space = optSpace.get().getId();
        var documents = databaseDocumentStorage.getDocuments(space);
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

        Long mapId = databaseSpaceStorage.createMindMap(space);

        if (topics.isEmpty()) {
            databaseSpaceStorage.updateMindMapStatus(spaceId, MindMapStatus.DONE);
            return RepeatStatus.FINISHED;
        }
        databaseSpaceStorage.updateMindMapStatus(spaceId, MindMapStatus.GENERATING);

        MindMap mindMap = mindMapClient.extractMindMap(topics);

        databaseSpaceStorage.createMindMapNodes(spaceId, mapId, mindMap);

        databaseSpaceStorage.updateMindMapStatus(spaceId, MindMapStatus.DONE);

        return RepeatStatus.FINISHED;
    }
}
