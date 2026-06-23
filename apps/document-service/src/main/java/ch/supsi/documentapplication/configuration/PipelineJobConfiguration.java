package ch.supsi.documentapplication.configuration;

import ch.supsi.documentapplication.model.Chunk;
import ch.supsi.documentapplication.model.ChunkDeserializer;
import ch.supsi.documentapplication.service.clients.LLMInputSanitizer;
import ch.supsi.documentapplication.service.clients.StringSanitizer;
import ch.supsi.documentapplication.service.processingpipeline.CleanupTasklet;
import ch.supsi.documentapplication.service.processingpipeline.EnrichmentTasklet;
import ch.supsi.documentapplication.service.processingpipeline.JSONOutputTasklet;
import ch.supsi.documentapplication.service.processingpipeline.OCRTasklet;
import ch.supsi.documentapplication.service.processingpipeline.SegmentationTasklet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
    public StringSanitizer stringSanitizer() {
        return new LLMInputSanitizer();
    }

    @Bean
    public ObjectMapper chunkMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Chunk.class, new ChunkDeserializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

    @Bean
    public LanguageDetector languageDetector() {
        return LanguageDetectorBuilder.fromAllLanguages().build();
    }

    @Bean
    public Job pipelineJob(
            JobRepository jobRepository, Step ocr, Step segment, Step enrich, Step writeJSON, Step cleanup) {
        return new JobBuilder("documentProcessingJob", jobRepository)
                .start(ocr)
                .next(segment)
                .next(enrich)
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

        var stepBuilder = new StepBuilder(stepName, jobRepository)
                .tasklet(tasklet, txManager)
                .build();

        return stepBuilder;
    }
}
