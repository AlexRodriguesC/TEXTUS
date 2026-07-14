package ch.supsi.documentapplication.configuration.job;

import ch.supsi.documentapplication.service.async.mindmappipeline.GenerationTasklet;
import ch.supsi.documentapplication.service.async.mindmappipeline.SetupTasklet;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MindMapJobConfiguration extends JobConfiguration {

    private static final String JOB_NAME = "mind_map_creation_job";

    @Bean
    public Job mindMapJob(JobRepository jobRepository, Step mapSetup, Step generate) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(mapSetup)
                .next(generate)
                .build();
    }

    @Bean
    public Step mapSetup(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("mindMapSetup") SetupTasklet setupTasklet) {
        return buildTaskletStep(SetupTasklet.class.getName(), jobRepository, transactionManager, setupTasklet);
    }

    @Bean
    public Step generate(
            JobRepository jobRepository, PlatformTransactionManager txManager, GenerationTasklet generationTasklet) {
        return buildTaskletStep(GenerationTasklet.class.getSimpleName(), jobRepository, txManager, generationTasklet);
    }
}
