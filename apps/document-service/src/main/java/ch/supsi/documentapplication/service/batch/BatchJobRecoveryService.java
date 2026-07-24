package ch.supsi.documentapplication.service.batch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.batch.autoconfigure.BatchTaskExecutor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class BatchJobRecoveryService {

    private static final List<String> RECOVERABLE_JOB_NAMES =
            List.of("document_processing_job", "chapter_creation_job", "mind_map_creation_job");

    private static final int JOB_INSTANCE_PAGE_SIZE = 100;

    private final JobRepository jobRepository;
    private final JobOperator jobOperator;
    private final TaskExecutor batchTaskExecutor;

    public BatchJobRecoveryService(
            JobRepository jobRepository, JobOperator jobOperator, @BatchTaskExecutor TaskExecutor batchTaskExecutor) {
        this.jobRepository = jobRepository;
        this.jobOperator = jobOperator;
        this.batchTaskExecutor = batchTaskExecutor;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void recoverInterruptedJobsOnStartup() {
        batchTaskExecutor.execute(this::recoverAllInterruptedJobs);
    }

    void recoverAllInterruptedJobs() {
        Set<Long> recoveredExecutionIds = new HashSet<>();

        for (String jobName : RECOVERABLE_JOB_NAMES) {
            recoverInterruptedJobs(jobName, recoveredExecutionIds);
        }
    }

    /**
     * Recovers a stuck execution if possible. UNKNOWN executions cannot be
     * recovered/restarted (their state cannot be reliably determined), so callers
     * must handle that case separately via {@link #abandonUnrecoverable(JobExecution)}.
     */
    public JobExecution recoverIfNeeded(JobExecution jobExecution) {
        if (isRecoverable(jobExecution.getStatus())) {
            return jobOperator.recover(jobExecution);
        }

        return jobExecution;
    }

    public JobExecution recoverAndRestart(JobExecution jobExecution) throws JobRestartException {
        JobExecution recovered = recoverIfNeeded(jobExecution);
        return jobOperator.restart(recovered);
    }

    public JobExecution abandonUnrecoverable(JobExecution jobExecution) throws JobExecutionAlreadyRunningException {
        return jobOperator.abandon(jobExecution);
    }

    private void recoverInterruptedJobs(String jobName, Set<Long> recoveredExecutionIds) {
        int page = 0;
        List<JobInstance> instances;

        do {
            instances = jobRepository.getJobInstances(jobName, page * JOB_INSTANCE_PAGE_SIZE, JOB_INSTANCE_PAGE_SIZE);

            for (JobInstance instance : instances) {
                JobExecution execution = jobRepository.getLastJobExecution(instance);
                if (execution == null || !isInterrupted(execution.getStatus())) {
                    continue;
                }

                if (!recoveredExecutionIds.add(execution.getId())) {
                    continue;
                }

                handleInterruptedExecution(jobName, execution);
            }

            page++;
        } while (instances.size() == JOB_INSTANCE_PAGE_SIZE);
    }

    private void handleInterruptedExecution(String jobName, JobExecution execution) {
        BatchStatus status = execution.getStatus();

        if (status == BatchStatus.UNKNOWN) {
            // UNKNOWN executions have an indeterminate final state and cannot be
            // safely recovered or restarted by the framework; mark them ABANDONED.
            try {
                abandonUnrecoverable(execution);
                log.warn(
                        "Abandoned {} execution {} with UNKNOWN status after application restart (not restartable)",
                        jobName,
                        execution.getId());
            } catch (Exception e) {
                log.error(
                        "Failed to abandon {} execution {} with UNKNOWN status after application restart",
                        jobName,
                        execution.getId(),
                        e);
            }
            return;
        }

        try {
            log.warn("Recovering interrupted {} execution {} after application restart", jobName, execution.getId());
            recoverAndRestart(execution);
        } catch (Exception e) {
            try {
                if (execution.isRunning()) {
                    jobOperator.stop(execution);
                }
                log.error("Stopped {} execution {} after application restart", jobName, execution.getId(), e);
            } catch (Exception e2) {
                try {
                    abandonUnrecoverable(execution);
                    log.error(
                            "Abandoned {} execution {} after application restart (could not stop or restart)",
                            jobName,
                            execution.getId(),
                            e2);
                } catch (Exception e3) {
                    log.error(
                            "Failed to recover, stop, or abandon {} execution {} after application restart",
                            jobName,
                            execution.getId(),
                            e3);
                }
            }
        }
    }

    private static boolean isRecoverable(BatchStatus status) {
        return status == BatchStatus.STARTED || status == BatchStatus.STOPPING;
    }

    private static boolean isInterrupted(BatchStatus status) {
        return status == BatchStatus.STARTED || status == BatchStatus.STOPPING || status == BatchStatus.UNKNOWN;
    }
}
