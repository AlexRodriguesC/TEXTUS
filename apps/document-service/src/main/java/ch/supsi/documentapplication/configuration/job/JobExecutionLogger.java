package ch.supsi.documentapplication.configuration.job;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobExecutionLogger implements JobExecutionListener {

    @Override
    public void beforeJob(@NotNull JobExecution jobExecution) {
        log.info("Job execution started for {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(@NotNull JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        var startTime = jobExecution.getStartTime();
        var endTime = jobExecution.getEndTime();

        if (startTime == null || endTime == null) {
            log.warn("Job '{}' finished but start or end time is missing.", jobName);
            return;
        }

        Duration duration = Duration.between(startTime, endTime);
        log.info(
                "Job execution ended for {}. Duration: {}",
                jobExecution.getJobInstance().getJobName(),
                formatDuration(duration));
    }

    private String formatDuration(Duration d) {
        long seconds = d.getSeconds();
        long nanos = d.getNano();
        if (seconds < 60) {
            return String.format("%d.%03ds", seconds, nanos / 1_000_000);
        }
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%dm %ds", minutes, seconds);
    }
}
