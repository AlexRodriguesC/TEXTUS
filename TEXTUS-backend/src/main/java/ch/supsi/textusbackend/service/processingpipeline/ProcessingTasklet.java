package ch.supsi.textusbackend.service.processingpipeline;

import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Value;

public abstract class ProcessingTasklet implements Tasklet {

    @Value("#{jobParameters['DOCUMENT_ID']}") // Injected from job params
    protected Long documentId;
}
