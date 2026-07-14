package ch.supsi.documentapplication.service.async;

import java.util.UUID;
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
