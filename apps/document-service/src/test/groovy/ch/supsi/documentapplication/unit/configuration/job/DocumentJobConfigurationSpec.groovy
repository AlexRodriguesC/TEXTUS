package ch.supsi.documentapplication.unit.configuration.job

import ch.supsi.documentapplication.configuration.job.DocumentJobConfiguration
import ch.supsi.documentapplication.service.async.documentpipeline.CleanupTasklet
import ch.supsi.documentapplication.service.async.documentpipeline.DocumentTaskStepListener
import ch.supsi.documentapplication.service.async.documentpipeline.OCRTasklet
import ch.supsi.documentapplication.service.async.documentpipeline.SegmentationTasklet
import ch.supsi.documentapplication.service.async.documentpipeline.SetupTasklet
import ch.supsi.documentapplication.service.async.documentpipeline.SummarizationTasklet
import ch.supsi.documentapplication.service.async.documentpipeline.TopicExtractionTasklet
import org.springframework.batch.core.job.AbstractJob
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.transaction.PlatformTransactionManager
import spock.lang.Specification

class DocumentJobConfigurationSpec extends Specification {

    DocumentJobConfiguration configuration = new DocumentJobConfiguration()
    JobRepository jobRepository = Mock()
    PlatformTransactionManager transactionManager = Mock()
    DocumentTaskStepListener documentTaskStepListener = Mock()

    def "should run document processing steps in segmentation OCR summarization topic order"() {
        given:
        Step docSetup = configuration.docSetup(jobRepository, transactionManager, Mock(SetupTasklet))
        Step ocr = configuration.ocr(jobRepository, transactionManager, Mock(OCRTasklet), documentTaskStepListener)
        Step segment = configuration.segment(
                jobRepository, transactionManager, Mock(SegmentationTasklet), documentTaskStepListener)
        Step summarize = configuration.summarize(
                jobRepository, transactionManager, Mock(SummarizationTasklet), documentTaskStepListener)
        Step extractTopics = configuration.extractTopics(
                jobRepository, transactionManager, Mock(TopicExtractionTasklet), documentTaskStepListener)
        Step cleanup = configuration.cleanup(jobRepository, transactionManager, Mock(CleanupTasklet))

        when:
        Job job = configuration.documentJob(jobRepository, docSetup, ocr, segment, summarize, extractTopics, cleanup)

        then:
        job.name == "document_processing_job"
        def stepNames = (job as AbstractJob).stepNames as List
        stepNames.size() == 6
        stepNames[0] == SetupTasklet.class.name
        stepNames[1] == SegmentationTasklet.class.simpleName
        stepNames[2] == OCRTasklet.class.simpleName
        stepNames[3].startsWith(SummarizationTasklet.class.simpleName)
        stepNames[4].startsWith(TopicExtractionTasklet.class.simpleName)
        stepNames[5] == CleanupTasklet.class.simpleName
    }
}
