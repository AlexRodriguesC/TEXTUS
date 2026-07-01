package ch.supsi.documentapplication.service;

import java.util.List;
import java.util.UUID;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;

@SuppressWarnings("resolve")
public abstract class ProcessingTasklet implements Tasklet {

    @Value("#{jobParameters['DOCUMENT_ID']}")
    protected String documentId;

    @Value("#{jobParameters['DATABASE_DOCUMENT_ID']}")
    protected Long databaseDocumentId;

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

    @SuppressWarnings("unchecked")
    protected <E> List<E> getListValue(
            ExecutionContext executionContext, String key, List<E> defaultValue, Class<E> elementClass) {

        if (!executionContext.containsKey(key)) {
            return defaultValue;
        }

        Object value = executionContext.get(key);
        if (!(value instanceof List<?> list)) {
            return defaultValue;
        }

        for (Object item : list) {
            if (item != null && !elementClass.isInstance(item)) {
                return defaultValue;
            }
        }

        return (List<E>) list;
    }
}
