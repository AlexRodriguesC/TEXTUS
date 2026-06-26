package ch.supsi.documentapplication.configuration;

import ch.supsi.documentapplication.service.mindmappipeline.GenerationTasklet;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MindMapJobConfiguration {

    @Bean
    public Job mindMapJob(JobRepository jobRepository, Step generate) {
        return new JobBuilder("mindMapJob", jobRepository).start(generate).build();
    }

    @Bean
    public Step generate(
            JobRepository jobRepository, PlatformTransactionManager txManager, GenerationTasklet generationTasklet) {
        return new StepBuilder("generate", jobRepository)
                .tasklet(generationTasklet, txManager)
                .build();
    }
}
