package ch.supsi.documentapplication.service.async.mindmappipeline;

import ch.supsi.documentapplication.dataaccess.DatabaseSpaceStorage;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Log4j2
@Component(value = "mindMapSetup")
@StepScope
public class SetupTasklet implements Tasklet {

    @Value("#{jobParameters['SPACE_ID']}")
    protected Long spaceId;

    @Value("#{stepExecution.jobExecution.id}")
    private Long executionId;

    private final DatabaseSpaceStorage databaseSpaceStorage;

    public SetupTasklet(DatabaseSpaceStorage databaseSpaceStorage) {
        this.databaseSpaceStorage = databaseSpaceStorage;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws Exception {

        var optSpace = databaseSpaceStorage.read(spaceId);
        if (optSpace.isEmpty()) {
            var message = String.format("No space found with id %s", spaceId);
            log.error(message);
            contribution.setExitStatus(ExitStatus.FAILED);
            return RepeatStatus.FINISHED;
        }

        Long mapId = databaseSpaceStorage.createMindMap(optSpace.get().getId(), executionId);
        chunkContext
                .getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("MAP_ID", mapId);

        return null;
    }
}
