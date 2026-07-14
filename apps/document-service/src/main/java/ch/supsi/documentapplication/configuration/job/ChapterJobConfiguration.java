package ch.supsi.documentapplication.configuration.job;

import ch.supsi.documentapplication.service.async.learningpipeline.ChapterGenerationTasklet;
import ch.supsi.documentapplication.service.async.learningpipeline.SetupTasklet;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ChapterJobConfiguration extends JobConfiguration {

    private static final String JOB_NAME = "chapter_creation_job";

    @Bean
    public Job chapterJob(JobRepository jobRepository, Step setupChapters, Step generateChapters) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(setupChapters)
                .next(generateChapters)
                .build();
    }

    @Bean
    public Step generateChapters(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ChapterGenerationTasklet chapterGenerationTasklet) {
        return buildTaskletStep(
                ChapterGenerationTasklet.class.getName(), jobRepository, transactionManager, chapterGenerationTasklet);
    }

    @Bean
    public Step setupChapters(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("chapterSetup") SetupTasklet chapterSetupTasklet) {
        return buildTaskletStep(SetupTasklet.class.getName(), jobRepository, transactionManager, chapterSetupTasklet);
    }
}
