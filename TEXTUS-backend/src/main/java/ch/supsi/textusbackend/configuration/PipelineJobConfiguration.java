package ch.supsi.textusbackend.configuration;

import ch.supsi.textusbackend.service.processingpipeline.CleanupTasklet;
import ch.supsi.textusbackend.service.processingpipeline.EnrichmentTasklet;
import ch.supsi.textusbackend.service.processingpipeline.JSONOutputTasklet;
import ch.supsi.textusbackend.service.processingpipeline.KeyBasedMergingTasklet;
import ch.supsi.textusbackend.service.processingpipeline.OCRTasklet;
import ch.supsi.textusbackend.service.processingpipeline.SegmentationTasklet;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PipelineJobConfiguration {

    @Bean
    public TokenCountEstimator tokenCountEstimator() {
        return new JTokkitTokenCountEstimator();
    }

    @Bean
    public LanguageDetector languageDetector() {
        return LanguageDetectorBuilder.fromAllLanguages().build();
    }

    @Bean
    public Job pipelineJob(
            JobRepository jobRepository,
            Step ocr,
            Step segment,
            Step enrich,
            Step keyBasedMerging,
            Step writeJSON,
            Step cleanup) {
        return new JobBuilder("bookProcessingJob", jobRepository)
                .start(ocr)
                .next(segment)
                .next(enrich)
                .next(keyBasedMerging)
                .next(writeJSON)
                .next(cleanup)
                .build();
    }

    @Bean
    public Step ocr(JobRepository jobRepository, PlatformTransactionManager transactionManager, OCRTasklet ocrTasklet) {
        return buildTaskletStep("ocrTasklet", jobRepository, transactionManager, ocrTasklet);
    }

    @Bean
    public Step segment(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SegmentationTasklet segmentationTasklet) {
        return buildTaskletStep("segmentationTasklet", jobRepository, transactionManager, segmentationTasklet);
    }

    @Bean
    public Step enrich(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            EnrichmentTasklet enrichmentTasklet) {
        return buildTaskletStep("enrichmentTasklet", jobRepository, transactionManager, enrichmentTasklet);
    }

    @Bean
    public Step keyBasedMerging(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            KeyBasedMergingTasklet keyBasedMergingTasklet) {
        return buildTaskletStep("keyBasedMergingTasklet", jobRepository, transactionManager, keyBasedMergingTasklet);
    }

    @Bean
    public Step writeJSON(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JSONOutputTasklet jsonOutputTasklet) {
        return buildTaskletStep("writeJSON", jobRepository, transactionManager, jsonOutputTasklet);
    }

    @Bean
    public Step cleanup(
            JobRepository jobRepository, PlatformTransactionManager txManager, CleanupTasklet cleanupTasklet) {
        return buildTaskletStep("cleanup", jobRepository, txManager, cleanupTasklet);
    }

    private TaskletStep buildTaskletStep(
            String stepName, JobRepository jobRepository, PlatformTransactionManager txManager, Tasklet tasklet) {
        return new StepBuilder(stepName, jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }
}
