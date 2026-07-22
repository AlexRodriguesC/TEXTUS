package ch.supsi.documentapplication.service.batch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
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

    public JobExecution recoverIfNeeded(JobExecution jobExecution) {
        if (isInterrupted(jobExecution.getStatus())) {
            return jobOperator.recover(jobExecution);
        }

        return jobExecution;
    }

    public JobExecution recoverAndRestart(JobExecution jobExecution) throws JobRestartException {
        JobExecution recovered = recoverIfNeeded(jobExecution);
        return jobOperator.restart(recovered);
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

                try {
                    log.warn(
                            "Recovering interrupted {} execution {} after application restart",
                            jobName,
                            execution.getId());
                    recoverAndRestart(execution);
                } catch (Exception e) {
                    try {
                        if (execution.isRunning()) {
                            jobOperator.stop(execution);
                        }
                        log.error("Stopped {} execution {} after application restart", jobName, execution.getId(), e);
                    } catch (Exception e2) {
                        log.error(
                                "Failed to recover {} execution {} after application restart",
                                jobName,
                                execution.getId(),
                                e2);
                    }
                }
            }

            page++;
        } while (instances.size() == JOB_INSTANCE_PAGE_SIZE);
    }

    private static boolean isInterrupted(BatchStatus status) {
        return status == BatchStatus.STARTED || status == BatchStatus.STOPPING || status == BatchStatus.UNKNOWN;
    }
}
