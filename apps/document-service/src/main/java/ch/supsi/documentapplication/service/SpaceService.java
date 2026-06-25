package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.exception.DocumentDomainException;
import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.exception.UnauthorizedException;
import ch.supsi.documentapplication.model.MindMap;
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
    public SpaceService(IDatabaseSpaceStorage spaceStorage, JobOperator asyncJobOperator,
                        Job mindMapJob) {
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
        var spaces = spaceStorage.readAll(username);
        boolean found = spaces.stream().anyMatch(space -> Objects.equals(space.getId(), spaceId));
        if (!found) {
            return;
        }
        spaceStorage.update(spaceId, name, description);
    }

    @Override
    public void createMindMap(String username, Long spaceId) {
        var space = spaceStorage.read(spaceId);
        if (space == null) {
            throw NotFoundException.spaceNotFoundException(spaceId);
        }
        if (!space.getOwner().equals(username)) {
            throw UnauthorizedException.userNotOwnerOfSpace(username, spaceId);
        }

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("SPACE_ID", spaceId.toString())
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
        return null;
    }
}
