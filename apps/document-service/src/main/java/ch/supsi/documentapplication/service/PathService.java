package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.dataaccess.IDatabaseLearningStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.exception.JobFailedToStartException;
import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.exception.UnauthorizedException;
import ch.supsi.documentapplication.model.PathSettings;
import ch.supsi.documentapplication.model.Space;
import ch.supsi.documentapplication.model.learn.LearningPath;
import ch.supsi.documentapplication.model.learn.LearningPathStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class PathService implements IPathService {

    private final IDatabaseLearningStorage databaseLearningStorage;
    private final IDatabaseSpaceStorage databaseSpaceStorage;
    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final Job chapterJob;

    @Autowired
    public PathService(
            IDatabaseLearningStorage databaseLearningStorage,
            IDatabaseSpaceStorage databaseSpaceStorage,
            JobOperator jobOperator,
            JobRepository jobRepository,
            Job chapterJob) {
        this.databaseLearningStorage = databaseLearningStorage;
        this.databaseSpaceStorage = databaseSpaceStorage;
        this.jobOperator = jobOperator;
        this.jobRepository = jobRepository;
        this.chapterJob = chapterJob;
    }

    @Override
    public void createLearningPath(String owner, Long spaceId) {
        var space = getSpaceOrThrow(owner, spaceId);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("SPACE_ID", space.getId().toString())
                .addLong("TRIGGERED_AT", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobOperator.start(chapterJob, jobParameters);
        } catch (Exception e) {
            String message = String.format("Failed to start job %s", chapterJob.getName());
            log.error(message, e);
            throw new JobFailedToStartException(message);
        }
    }

    @Override
    public LearningPath getLearningPath(String owner, Long spaceId) {
        var space = getSpaceOrThrow(owner, spaceId);
        return databaseLearningStorage.read(space.getId());
    }

    @Override
    public LearningPathStatus getLearningPathStatus(String owner, Long spaceId) {
        var learningPath = getLearningPath(owner, spaceId);

        if (learningPath == null) {
            return LearningPathStatus.NOT_CREATED;
        }

        var jobId = learningPath.getJobId();
        var jobExecution = jobRepository.getJobExecution(jobId);
        if (jobExecution == null) {
            return LearningPathStatus.NOT_CREATED;
        }

        var status = jobExecution.getStatus();

        return status.isRunning()
                ? LearningPathStatus.GENERATING
                : status.isUnsuccessful() ? LearningPathStatus.FAILED : LearningPathStatus.DONE;
    }

    @Override
    public void deleteLearningPath(String owner, Long spaceId) {
        var learningPath = getLearningPathOrThrow(owner, spaceId);

        JobExecution jobExecution = jobRepository.getJobExecution(learningPath.getJobId());
        if (jobExecution != null && jobExecution.isRunning()) {
            try {
                jobOperator.stop(jobExecution);
            } catch (Exception e) {
                String message = String.format("Failed to stop job %s", learningPath.getJobId());
                log.error(message, e);
            }
            log.info("Stopped job {} for learning path in space with id {}", learningPath.getJobId(), spaceId);
        }

        databaseLearningStorage.delete(spaceId);
    }

    @Override
    public void updateSettings(String owner, Long spaceId, PathSettings settings) {
        settings.validate();
        var space = getSpaceOrThrow(owner, spaceId);
        var learningPath = getLearningPathOrThrow(space.getOwner(), space.getId());

        if (learningPath.getSettings() == settings) {
            return;
        }

        databaseLearningStorage.updateSettings(space.getId(), settings);
    }

    private LearningPath getLearningPathOrThrow(String owner, Long spaceId) {
        var learningPath = getLearningPath(owner, spaceId);

        if (learningPath == null) {
            throw NotFoundException.learningPathNotFound(spaceId);
        }

        return learningPath;
    }

    private Space getSpaceOrThrow(String username, Long spaceId) {
        var optSpace = databaseSpaceStorage.read(spaceId);
        if (optSpace.isEmpty()) {
            throw NotFoundException.spaceNotFoundException(spaceId);
        }
        var space = optSpace.get();
        return validateOwner(username, space);
    }

    private Space validateOwner(String requester, Space space) {
        if (!space.getOwner().equals(requester)) {
            throw UnauthorizedException.userNotOwnerOfSpace(requester, space.getId());
        }
        return space;
    }
}
