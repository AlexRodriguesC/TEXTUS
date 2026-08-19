package ch.supsi.documentapplication.service.async.learningpipeline;

import ch.supsi.documentapplication.dataaccess.IDatabasePathStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.service.async.ProcessingTasklet;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Log4j2
@Component(value = "chapterSetup")
@StepScope
public class SetupTasklet extends ProcessingTasklet {

    @Value("#{jobParameters['SPACE_ID']}")
    protected Long spaceId;

    @Value("#{stepExecution.jobExecution.jobInstanceId}")
    private Long jobInstanceId;

    private final IDatabaseSpaceStorage databaseSpaceStorage;
    private final IDatabasePathStorage databaseLearningStorage;

    public SetupTasklet(IDatabaseSpaceStorage databaseSpaceStorage, IDatabasePathStorage databaseLearningStorage) {
        this.databaseSpaceStorage = databaseSpaceStorage;
        this.databaseLearningStorage = databaseLearningStorage;
    }

    @Override
    public @Nullable RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext)
            throws Exception {
        log.debug("Executing chapter generation setup step for space with spaceId {}", spaceId);

        var optSpace = databaseSpaceStorage.read(spaceId);
        if (optSpace.isEmpty()) {
            var message = String.format("No space found with id %s", spaceId);
            log.error(message);
            contribution.setExitStatus(ExitStatus.FAILED);
            return RepeatStatus.FINISHED;
        }

        Long pathId = databaseLearningStorage.createLearningPath(optSpace.get().getId(), jobInstanceId);
        chunkContext
                .getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("PATH_ID", pathId);

        log.debug("Finished chapter generation setup step for space with spaceId {}", spaceId);
        return RepeatStatus.FINISHED;
    }
}
