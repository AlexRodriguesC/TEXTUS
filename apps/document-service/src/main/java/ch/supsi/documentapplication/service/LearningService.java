package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.dataaccess.IDatabaseLearningStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.exception.JobFailedToStartException;
import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.exception.UnauthorizedException;
import ch.supsi.documentapplication.model.LearningPath;
import ch.supsi.documentapplication.model.LearningPathStatus;
import ch.supsi.documentapplication.model.Space;
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
public class LearningService implements ILearningService {

    private final IDatabaseLearningStorage databaseLearningStorage;
    private final IDatabaseSpaceStorage databaseSpaceStorage;
    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final Job chapterJob;

    @Autowired
    public LearningService(IDatabaseLearningStorage databaseLearningStorage, IDatabaseSpaceStorage databaseSpaceStorage, JobOperator jobOperator, JobRepository jobRepository, Job chapterJob) {
        this.databaseLearningStorage = databaseLearningStorage;
        this.databaseSpaceStorage = databaseSpaceStorage;
        this.jobOperator = jobOperator;
        this.jobRepository = jobRepository;
        this.chapterJob = chapterJob;
    }

    @Override
    public void createLearningPath(String username, Long spaceId) {
        var space = getSpaceOrThrow(username, spaceId);

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
    public LearningPath getLearningPath(String username, Long spaceId) {
        var space = getSpaceOrThrow(username, spaceId);
        return databaseLearningStorage.read(space.getId());
    }

    @Override
    public LearningPathStatus getLearningPathStatus(String username, Long spaceId) {
        var learningPath = getLearningPath(username, spaceId);

        if (learningPath == null) {
            return LearningPathStatus.NOT_CREATED;
        }

        var jobId = learningPath.getJobId();
        var jobExecution = jobRepository.getJobExecution(jobId);
        if (jobExecution == null) {
            return LearningPathStatus.NOT_CREATED;
        }

        var status = jobExecution.getStatus();

        return status.isRunning() ? LearningPathStatus.GENERATING : status.isUnsuccessful() ? LearningPathStatus.FAILED : LearningPathStatus.DONE;
    }

    @Override
    public void deleteLearningPath(String username, Long spaceId) {
        var learningPath = getLearningPath(username, spaceId);

        if (learningPath == null) {
            throw NotFoundException.learningPathNotFound(spaceId);
        }

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
