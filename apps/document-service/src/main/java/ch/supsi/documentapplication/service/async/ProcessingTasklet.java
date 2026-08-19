<<<<<<<< HEAD:apps/document-service/src/main/java/ch/supsi/documentapplication/service/processingpipeline/ProcessingTasklet.java
package ch.supsi.documentapplication.service.processingpipeline;
========
package ch.supsi.documentapplication.service.async;
>>>>>>>> origin/feature/testing_service:apps/document-service/src/main/java/ch/supsi/documentapplication/service/async/ProcessingTasklet.java

import ch.supsi.documentapplication.service.async.documentpipeline.SetupTasklet;
import java.util.UUID;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;

public abstract class ProcessingTasklet implements Tasklet {

    @Value("#{jobParameters['DOCUMENT_ID']}")
    protected String documentId;

    @Value("#{jobParameters['SPACE_ID']}")
    protected Long spaceId;

    protected UUID getDocumentIdAsUUID() {
        return UUID.fromString(documentId);
    }

    protected Long getDatabaseDocumentId(ChunkContext chunkContext) {
        return (Long) chunkContext.getStepContext().getJobExecutionContext().get(SetupTasklet.DATABASE_DOCUMENT_ID);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getValue(ExecutionContext executionContext, String key, T defaultValue, Class<T> clazz) {
        if (!executionContext.containsKey(key)) {
            return defaultValue;
        }

        Object value = executionContext.get(key);
        if (!clazz.isInstance(value)) {
            return defaultValue;
        }

        return (T) value;
    }
}
