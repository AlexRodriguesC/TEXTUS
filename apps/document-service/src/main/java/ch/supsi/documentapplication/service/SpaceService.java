package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.controller.DTO.response.MindMapSummaryDTO;
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.exception.JobFailedToStartException;
import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.MindMapStatus;
import ch.supsi.documentapplication.model.Space;
import ch.supsi.documentapplication.service.batch.JobExecutionResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.JobInterruptedException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class SpaceService extends ch.supsi.documentapplication.service.Service implements ISpaceService {

    private final JobOperator jobOperator;
    private final Job mindMapJob;
    private final JobRepository jobRepository;

    @Autowired
    public SpaceService(
            IDatabaseSpaceStorage spaceStorage,
            JobOperator asyncJobOperator,
            Job mindMapJob,
            JobRepository jobRepository) {
        super(spaceStorage);
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
    public MindMapSummaryDTO createMindMap(String username, Long spaceId) {
        var space = getSpaceOrThrow(username, spaceId);

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("SPACE_ID", space.getId().toString())
                .addLong("TRIGGERED_AT", System.currentTimeMillis())
                .toJobParameters();

        JobInstance jobInstance = jobRepository.createJobInstance(mindMapJob.getName(), jobParameters);
        Long mapId = spaceStorage.createMindMap(space.getId(), jobInstance.getInstanceId());

        ExecutionContext executionContext = new ExecutionContext();
        executionContext.put("MAP_ID", mapId);
        JobExecution jobExecution = jobRepository.createJobExecution(jobInstance, jobParameters, executionContext);

        try {
            mindMapJob.execute(jobExecution);
        } catch (JobInterruptedException e) {
            // Execute already handles the task state update gracefully in this case
            log.warn("Job for mind map in space with id {} was interrupted", spaceId, e);
            throw new JobFailedToStartException("Job for mind map in space with id " + spaceId + " was interrupted");
        }

        return new MindMapSummaryDTO(mapId, MindMapStatus.GENERATING);
    }

    @Override
    public List<MindMapSummaryDTO> listMindMaps(String username, Long spaceId) {
        var space = getSpaceOrThrow(username, spaceId);
        var mindMap = spaceStorage.readMindMap(space.getId());
        List<MindMapSummaryDTO> mindMaps = new ArrayList<>();
        if (mindMap != null && mindMap.getId() != null) {
            mindMaps.add(new MindMapSummaryDTO(mindMap.getId(), resolveStatus(mindMap)));
        }
        return mindMaps;
    }

    @Override
    public @Nullable MindMap getMindMap(String username, Long spaceId) {
        var space = getSpaceOrThrow(username, spaceId);
        return spaceStorage.readMindMap(space.getId());
    }

    @Override
    public MindMap getMindMap(String username, Long spaceId, Long mindMapId) {
        return getMindMapOrThrow(username, spaceId, mindMapId);
    }

    @Override
    public MindMapStatus getMindMapStatus(String username, Long spaceId) {
        var mindMap = getMindMap(username, spaceId);
        return resolveStatus(mindMap);
    }

    @Override
    public void deleteMindMap(String username, Long spaceId, Long mindMapId) {
        var mindMap = getMindMapOrThrow(username, spaceId, mindMapId);

        JobExecution jobExecution = JobExecutionResolver.getLatestJobExecution(jobRepository, mindMap.getJobId());
        if (jobExecution != null && jobExecution.isRunning()) {
            try {
                jobOperator.stop(jobExecution);
            } catch (Exception e) {
                String message = String.format("Failed to stop job %s", mindMap.getJobId());
                log.error(message, e);
            }
            log.info("Stopped job {} for mind map in space with id {}", mindMap.getJobId(), spaceId);
        }

        spaceStorage.deleteMindMap(spaceId);
    }

    private MindMapStatus resolveStatus(@Nullable MindMap mindMap) {
        if (mindMap == null || mindMap.getJobId() == null) {
            return MindMapStatus.NOT_CREATED;
        }

        var jobExecution = JobExecutionResolver.getLatestJobExecution(jobRepository, mindMap.getJobId());
        if (jobExecution == null) {
            return MindMapStatus.NOT_CREATED;
        }

        var status = jobExecution.getStatus();
        return status.isRunning()
                ? MindMapStatus.GENERATING
                : status.isUnsuccessful() ? MindMapStatus.FAILED : MindMapStatus.DONE;
    }

    private MindMap getMindMapOrThrow(String username, Long spaceId, Long mindMapId) {
        var mindMap = getMindMap(username, spaceId);

        if (mindMap == null || mindMap.getId() == null || !Objects.equals(mindMap.getId(), mindMapId)) {
            throw NotFoundException.mindMapNotFoundException(spaceId);
        }

        return mindMap;
    }
}
