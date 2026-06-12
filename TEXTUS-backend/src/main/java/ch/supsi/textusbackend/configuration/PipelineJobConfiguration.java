package ch.supsi.textusbackend.configuration;

import ch.supsi.textusbackend.service.processingpipeline.ChunkContentTasklet;
import ch.supsi.textusbackend.service.processingpipeline.CleanupTasklet;
import ch.supsi.textusbackend.service.processingpipeline.ExtractIndexTasklet;
import ch.supsi.textusbackend.service.processingpipeline.OCRTasklet;
import ch.supsi.textusbackend.service.processingpipeline.SummariseContentTasklet;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
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
    public LanguageDetector languageDetector() {
        return LanguageDetectorBuilder.fromAllLanguages().build();
    }

    @Bean
    public Job pipelineJob(
            JobRepository jobRepository,
            Step ocr,
            Step extractIndex,
            Step chunkContent,
            Step summariseContent,
            Step cleanup) {
        return new JobBuilder("bookProcessingJob", jobRepository)
                .start(ocr)
                .next(extractIndex)
                .next(chunkContent)
                .next(summariseContent)
                .next(cleanup)
                .build();
    }

    @Bean
    public Step ocr(JobRepository jobRepository, PlatformTransactionManager transactionManager, OCRTasklet ocrTasklet) {
        return buildTaskletStep("ocrTasklet", jobRepository, transactionManager, ocrTasklet);
    }

    @Bean
    public Step extractIndex(
            JobRepository jobRepository,
            PlatformTransactionManager txManager,
            ExtractIndexTasklet extractIndexTasklet) {
        return buildTaskletStep("extractIndex", jobRepository, txManager, extractIndexTasklet);
    }

    @Bean
    public Step chunkContent(
            JobRepository jobRepository,
            PlatformTransactionManager txManager,
            ChunkContentTasklet chunkContentTasklet) {
        return buildTaskletStep("chunkContent", jobRepository, txManager, chunkContentTasklet);
    }

    @Bean
    public Step summariseContent(
            JobRepository jobRepository,
            PlatformTransactionManager txManager,
            SummariseContentTasklet summariseContentTasklet) {
        return buildTaskletStep("summariseContent", jobRepository, txManager, summariseContentTasklet);
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
