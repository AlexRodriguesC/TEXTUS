package ch.supsi.documentapplication.configuration.job;

import ch.supsi.documentapplication.service.mindmappipeline.GenerationTasklet;
import ch.supsi.documentapplication.service.mindmappipeline.TopicExtractionTasklet;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MindMapJobConfiguration extends JobConfiguration {

    private static final String JOB_NAME = "mind_map_creation_job";

    @Autowired
    public MindMapJobConfiguration(JobExecutionLogger jobExecutionLogger) {
        super(jobExecutionLogger);
    }

    @Bean
    public Job mindMapJob(JobRepository jobRepository, Step extractTopics, Step generate) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(extractTopics)
                .next(generate)
                .build();
    }

    @Bean
    public Step extractTopics(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            TopicExtractionTasklet topicExtractionTasklet) {
        return buildTaskletStep(
                topicExtractionTasklet.getClass().getSimpleName(),
                jobRepository,
                transactionManager,
                topicExtractionTasklet);
    }

    @Bean
    public Step generate(
            JobRepository jobRepository, PlatformTransactionManager txManager, GenerationTasklet generationTasklet) {
        return buildTaskletStep(GenerationTasklet.class.getSimpleName(), jobRepository, txManager, generationTasklet);
    }
}
