package ch.supsi.documentapplication.configuration.job;

import ch.supsi.documentapplication.service.clients.LLMInputSanitizer;
import ch.supsi.documentapplication.service.clients.StringSanitizer;
import ch.supsi.documentapplication.service.documentpipeline.*;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DocumentJobConfiguration extends JobConfiguration {

    private static final String JOB_NAME = "document_processing_job";

    @Autowired
    public DocumentJobConfiguration(JobExecutionLogger jobExecutionLogger) {
        super(jobExecutionLogger);
    }

    @Bean
    public StringSanitizer stringSanitizer() {
        return new LLMInputSanitizer();
    }

    @Bean
    public LanguageDetector languageDetector() {
        return LanguageDetectorBuilder.fromAllLanguages().build();
    }

    @Bean
    public Job pipelineJob(JobRepository jobRepository, Step ocr, Step segment, Step summarize, Step cleanup) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(ocr)
                .next(segment)
                .next(summarize)
                .next(cleanup)
                .build();
    }

    @Bean
    public Step ocr(JobRepository jobRepository, PlatformTransactionManager transactionManager, OCRTasklet ocrTasklet) {
        return buildTaskletStep(OCRTasklet.class.getSimpleName(), jobRepository, transactionManager, ocrTasklet);
    }

    @Bean
    public Step segment(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SegmentationTasklet segmentationTasklet) {
        return buildTaskletStep(
                SegmentationTasklet.class.getSimpleName(), jobRepository, transactionManager, segmentationTasklet);
    }

    @Bean
    public Step summarize(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SummarizationTasklet summarizationTasklet) {
        return buildTaskletStep(
                summarizationTasklet.getClass().getSimpleName(),
                jobRepository,
                transactionManager,
                summarizationTasklet);
    }

    @Bean
    public Step cleanup(
            JobRepository jobRepository, PlatformTransactionManager txManager, CleanupTasklet cleanupTasklet) {
        return buildTaskletStep(CleanupTasklet.class.getSimpleName(), jobRepository, txManager, cleanupTasklet);
    }
}
