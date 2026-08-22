package ch.supsi.documentapplication.service.async.documentpipeline;

import ch.supsi.documentapplication.model.TaskType;
import org.jspecify.annotations.Nullable;

/**
 * Maps document pipeline Spring Batch step names to {@link TaskType}.
 * Step names may be CGLIB-enhanced (e.g. {@code TopicExtractionTasklet$$SpringCGLIB$$0}).
 */
public final class DocumentPipelineTaskType {

    private DocumentPipelineTaskType() {}

    public static @Nullable TaskType fromStepName(@Nullable String stepName) {
        if (stepName == null) {
            return null;
        }
        if (stepName.contains("OCRTasklet")) {
            return TaskType.OCR;
        }
        if (stepName.contains("SegmentationTasklet")) {
            return TaskType.SEGMENTATION;
        }
        if (stepName.contains("SummarizationTasklet")) {
            return TaskType.SUMMARIZATION;
        }
        if (stepName.contains("TopicExtractionTasklet")) {
            return TaskType.TOPIC;
        }
        return null;
    }
}
