package ch.supsi.documentapplication.service.async.documentpipeline;

import ch.supsi.documentapplication.model.TaskType;
import ch.supsi.documentapplication.service.document.DocumentTaskProgressService;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class DocumentTaskStepListener implements StepExecutionListener {

    private final DocumentTaskProgressService documentTaskProgressService;

    @Autowired
    public DocumentTaskStepListener(DocumentTaskProgressService documentTaskProgressService) {
        this.documentTaskProgressService = documentTaskProgressService;
    }

    @Override
    public ExitStatus afterStep(@NotNull StepExecution stepExecution) {
        TaskType taskType = resolveTaskType(stepExecution.getStepName());
        if (taskType == null) {
            return stepExecution.getExitStatus();
        }

        Long documentId =
                (Long) stepExecution.getJobExecution().getExecutionContext().get(SetupTasklet.DATABASE_DOCUMENT_ID);
        if (documentId == null) {
            return stepExecution.getExitStatus();
        }

        if (stepExecution.getExitStatus().getExitCode().equals(ExitStatus.FAILED.getExitCode())) {
            documentTaskProgressService.failTask(documentId, taskType);
        }

        return stepExecution.getExitStatus();
    }

    private TaskType resolveTaskType(String stepName) {
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
