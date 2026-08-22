package ch.supsi.documentapplication.unit.service.async.documentpipeline

import ch.supsi.documentapplication.model.TaskType
import ch.supsi.documentapplication.service.async.documentpipeline.DocumentPipelineTaskType
import spock.lang.Specification
import spock.lang.Unroll

class DocumentPipelineTaskTypeSpec extends Specification {

    @Unroll
    def "maps step name '#stepName' to #expected"() {
        expect:
        DocumentPipelineTaskType.fromStepName(stepName) == expected

        where:
        stepName                                       || expected
        "OCRTasklet"                                   || TaskType.OCR
        "SegmentationTasklet"                          || TaskType.SEGMENTATION
        "SummarizationTasklet"                         || TaskType.SUMMARIZATION
        "TopicExtractionTasklet"                       || TaskType.TOPIC
        "TopicExtractionTasklet\$\$SpringCGLIB\$\$0"   || TaskType.TOPIC
        "CleanupTasklet"                               || null
        null                                           || null
    }
}
