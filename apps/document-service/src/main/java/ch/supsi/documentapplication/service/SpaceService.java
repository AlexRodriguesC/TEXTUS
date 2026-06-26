package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.exception.DocumentDomainException;
import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.exception.UnauthorizedException;
import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.MindMapStatus;
import ch.supsi.documentapplication.model.Space;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class SpaceService implements ISpaceService {

    private final IDatabaseSpaceStorage spaceStorage;
    private final JobOperator jobOperator;
    private final Job mindMapJob;

    @Autowired
    public SpaceService(IDatabaseSpaceStorage spaceStorage, JobOperator asyncJobOperator, Job mindMapJob) {
        this.spaceStorage = spaceStorage;
        this.jobOperator = asyncJobOperator;
        this.mindMapJob = mindMapJob;
    }

    @Override
    public Long create(String owner, String name, String description) {
        return spaceStorage.create(owner, name, description);
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
    public void update(String username, Long spaceId, String name, String description) {
        var space = getSpaceOrThrow(username, spaceId);
        var updatedName = name == null || name.isEmpty() ? space.getName() : name;
        var updatedDescription = description == null || description.isEmpty() ? space.getDescription() : description;

        spaceStorage.update(space.getId(), updatedName, updatedDescription);
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
            throw DocumentDomainException.jobFailedException(e);
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
            throw NotFoundException.mindMapNotFoundException(spaceId);
        }

        return mindMap.getMindMapStatus();
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
