package ch.supsi.documentapplication.configuration.job;

import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.transaction.PlatformTransactionManager;

public abstract class JobConfiguration {

    protected TaskletStep buildTaskletStep(
            String stepName, JobRepository jobRepository, PlatformTransactionManager txManager, Tasklet tasklet) {

        // This allows steps to add the following keys to the job execution context, otherwise changes to the ctx from a
        // step
        // will not be persisted between steps
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[] {"MAP_ID", "DATABASE_DOCUMENT_ID", "PATH_ID"});

        return new StepBuilder(stepName, jobRepository)
                .tasklet(tasklet, txManager)
                .listener(listener)
                .build();
    }
}
