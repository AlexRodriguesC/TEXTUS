package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.exception.JobFailedToStartException;
import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.exception.UnauthorizedException;
import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.MindMapStatus;
import ch.supsi.documentapplication.model.Space;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SpaceService implements ISpaceService {

    private static final String JOB_NAME = "mind_map_creation_job";

    private final IDatabaseSpaceStorage spaceStorage;
    private final JobOperator jobOperator;
    private final Job mindMapJob;
    private final JobRepository jobRepository;

    @Autowired
    public SpaceService(IDatabaseSpaceStorage spaceStorage, JobOperator asyncJobOperator, Job mindMapJob, JobRepository jobRepository) {
        this.spaceStorage = spaceStorage;
        this.jobOperator = asyncJobOperator;
        this.mindMapJob = mindMapJob;
        this.jobRepository = jobRepository;
    }

    @Override
    public List<Space> getSpaces(String owner) {
        return spaceStorage.readAll(owner);
    }

    @Override
    public void delete(String username, Long spaceId) {
        var spaces = spaceStorage.readAll(username);
        boolean found = spaces.stream().anyMatch(space -> Objects.equals(space.getId(), spaceId));
        if (!found) {
            return;
        }
        spaceStorage.delete(spaceId);
    }

    @Override
    public Long create(Space space) {
        return spaceStorage.create(space);
    }

    @Override
    public void update(Space space) {
        var current = getSpaceOrThrow(space.getOwner(), space.getId());
        var updatedIcon = space.getIcon() == null || space.getIcon().isEmpty() ? current.getIcon() : space.getIcon();
        var updatedName = space.getName() == null || space.getName().isEmpty() ? current.getName() : space.getName();
        var updatedDescription =
                space.getDescription() == null || space.getDescription().isEmpty()
                        ? current.getDescription()
                        : space.getDescription();

        spaceStorage.update(
                new Space(current.getId(), current.getOwner(), updatedIcon, updatedName, updatedDescription));
    }

    @Override
    public void createMindMap(String username, Long spaceId) {
        var space = getSpaceOrThrow(username, spaceId);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("SPACE_ID", space.getId().toString())
                .addLong("TRIGGERED_AT", System.currentTimeMillis())
                .toJobParameters();

        try {
           jobOperator.start(mindMapJob, jobParameters);
        } catch (Exception e) {
            String message = String.format("Failed to start job %s", mindMapJob.getName());
            log.error(message, e);
            throw new JobFailedToStartException(message);
        }
    }

    @Override
    public @Nullable MindMap getMindMap(String username, Long spaceId) {
        var space = getSpaceOrThrow(username, spaceId);

        return spaceStorage.readMindMap(space.getId());
    }

    @Override
    public MindMapStatus getMindMapStatus(String username, Long spaceId) {
        var mindMap = getMindMap(username, spaceId);
        if (mindMap == null) {
            return MindMapStatus.NOT_CREATED;
        }

        JobInstance jobInstance = jobRepository.getLastJobInstance(JOB_NAME);
        if (jobInstance == null) {
            return MindMapStatus.NOT_CREATED;
        }

        Optional<JobExecution> lastExecution = Optional.ofNullable(jobRepository.getLastJobExecution(jobInstance));

        if (lastExecution.isEmpty()) {
            return MindMapStatus.NOT_CREATED;
        }

        JobExecution jobExecution = lastExecution.get();
        var status = jobExecution.getStatus();

        return status.isRunning() ? MindMapStatus.GENERATING : status.isUnsuccessful() ? MindMapStatus.FAILED : MindMapStatus.DONE;
    }

    private Space getSpaceOrThrow(String username, Long spaceId) {
        var optSpace = spaceStorage.read(spaceId);
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
