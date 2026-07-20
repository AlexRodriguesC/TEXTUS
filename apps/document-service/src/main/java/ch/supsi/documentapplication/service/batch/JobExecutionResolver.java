package ch.supsi.documentapplication.service.batch;

import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.repository.JobRepository;

public final class JobExecutionResolver {

    private JobExecutionResolver() {}

    public static @Nullable JobExecution getLatestJobExecution(JobRepository jobRepository, Long jobInstanceId) {
        JobInstance jobInstance = jobRepository.getJobInstance(jobInstanceId);
        if (jobInstance == null) {
            return null;
        }

        return jobRepository.getLastJobExecution(jobInstance);
    }
}
