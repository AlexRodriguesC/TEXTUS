package ch.supsi.documentapplication.configuration.job;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.transaction.PlatformTransactionManager;

public abstract class JobConfiguration {

    protected TaskletStep buildTaskletStep(
            String stepName, JobRepository jobRepository, PlatformTransactionManager txManager, Tasklet tasklet) {

        return new StepBuilder(stepName, jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }
}
