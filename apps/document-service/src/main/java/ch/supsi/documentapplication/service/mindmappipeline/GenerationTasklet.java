package ch.supsi.documentapplication.service.mindmappipeline;

import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.MindMapStatus;
import ch.supsi.documentapplication.service.clients.mindmap.MindMapClient;
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

    @Value("#{jobParameters['SPACE_ID']}")
    protected Long spaceId;

    private final IDatabaseSpaceStorage databaseSpaceStorage;

    private final MindMapClient mindMapClient;

    @Autowired
    public GenerationTasklet(IDatabaseSpaceStorage databaseSpaceStorage, MindMapClient mindMapClient) {
        this.databaseSpaceStorage = databaseSpaceStorage;
        this.mindMapClient = mindMapClient;
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

        var space = optSpace.get();

        var chunks = space.getChunks();

        if (chunks == null || chunks.isEmpty()) {
            databaseSpaceStorage.updateMindMapStatus(spaceId, MindMapStatus.DONE);
            return RepeatStatus.FINISHED;
        }

        databaseSpaceStorage.updateMindMapStatus(spaceId, MindMapStatus.GENERATING);

        MindMap mindMap = mindMapClient.extractMindMap(chunks);

        databaseSpaceStorage.createMindMap(spaceId, mindMap);
        databaseSpaceStorage.updateMindMapStatus(spaceId, MindMapStatus.DONE);

        return RepeatStatus.FINISHED;
    }
}
