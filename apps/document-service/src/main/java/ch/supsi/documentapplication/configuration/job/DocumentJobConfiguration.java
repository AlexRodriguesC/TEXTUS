package ch.supsi.documentapplication.configuration.job;

import ch.supsi.documentapplication.service.async.documentpipeline.*;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DocumentJobConfiguration extends JobConfiguration {

    private static final String JOB_NAME = "document_processing_job";

    @Bean
    public LanguageDetector languageDetector() {
        return LanguageDetectorBuilder.fromAllLanguages().build();
    }

    @Bean
    public Job documentJob(
            JobRepository jobRepository,
            Step docSetup,
            Step ocr,
            Step segment,
            Step summarize,
            Step extractTopics,
            Step cleanup) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(docSetup)
                .next(ocr)
                .next(segment)
                .next(summarize)
                .next(extractTopics)
                .next(cleanup)
                .build();
    }

    @Bean
    public Step docSetup(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("documentSetup") SetupTasklet setupTasklet) {
        return buildTaskletStep(SetupTasklet.class.getName(), jobRepository, transactionManager, setupTasklet);
    }

    @Bean
    public Step ocr(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            OCRTasklet ocrTasklet,
            DocumentTaskStepListener documentTaskStepListener) {
        return buildTaskletStep(
                OCRTasklet.class.getSimpleName(),
                jobRepository,
                transactionManager,
                ocrTasklet,
                documentTaskStepListener);
    }

    @Bean
    public Step segment(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SegmentationTasklet segmentationTasklet,
            DocumentTaskStepListener documentTaskStepListener) {
        return buildTaskletStep(
                SegmentationTasklet.class.getSimpleName(),
                jobRepository,
                transactionManager,
                segmentationTasklet,
                documentTaskStepListener);
    }

    @Bean
    public Step summarize(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SummarizationTasklet summarizationTasklet,
            DocumentTaskStepListener documentTaskStepListener) {
        return buildTaskletStep(
                summarizationTasklet.getClass().getSimpleName(),
                jobRepository,
                transactionManager,
                summarizationTasklet,
                documentTaskStepListener);
    }

    @Bean
    public Step extractTopics(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            TopicExtractionTasklet topicExtractionTasklet,
            DocumentTaskStepListener documentTaskStepListener) {
        return buildTaskletStep(
                topicExtractionTasklet.getClass().getSimpleName(),
                jobRepository,
                transactionManager,
                topicExtractionTasklet,
                documentTaskStepListener);
    }

    @Bean
    public Step cleanup(
            JobRepository jobRepository, PlatformTransactionManager txManager, CleanupTasklet cleanupTasklet) {
        return buildTaskletStep(CleanupTasklet.class.getSimpleName(), jobRepository, txManager, cleanupTasklet);
    }
}
