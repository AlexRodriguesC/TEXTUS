package ch.supsi.documentapplication.service.batch;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class BatchJobShutdownCoordinator {

    private static final List<String> RECOVERABLE_JOB_NAMES =
            List.of("document_processing_job", "chapter_creation_job", "mind_map_creation_job");

    private static final int JOB_INSTANCE_PAGE_SIZE = 100;

    private final JobRepository jobRepository;
    private final JobOperator jobOperator;

    public BatchJobShutdownCoordinator(JobRepository jobRepository, JobOperator jobOperator) {
        this.jobRepository = jobRepository;
        this.jobOperator = jobOperator;
    }

    @EventListener(ContextClosedEvent.class)
    public void stopRunningJobsOnShutdown() {
        for (String jobName : RECOVERABLE_JOB_NAMES) {
            stopRunningJobs(jobName);
        }
    }

    private void stopRunningJobs(String jobName) {
        int page = 0;
        List<JobInstance> instances;

        do {
            instances = jobRepository.getJobInstances(jobName, page * JOB_INSTANCE_PAGE_SIZE, JOB_INSTANCE_PAGE_SIZE);

            for (JobInstance instance : instances) {
                JobExecution execution = jobRepository.getLastJobExecution(instance);
                if (execution == null || !isRunning(execution.getStatus())) {
                    continue;
                }

                try {
                    log.info("Stopping {} execution {} during application shutdown", jobName, execution.getId());
                    jobOperator.stop(execution);
                } catch (Exception e) {
                    log.warn(
                            "Failed to stop {} execution {} during application shutdown",
                            jobName,
                            execution.getId(),
                            e);
                }
            }

            page++;
        } while (instances.size() == JOB_INSTANCE_PAGE_SIZE);
    }

    private static boolean isRunning(BatchStatus status) {
        return status == BatchStatus.STARTED || status == BatchStatus.STARTING;
    }
}
