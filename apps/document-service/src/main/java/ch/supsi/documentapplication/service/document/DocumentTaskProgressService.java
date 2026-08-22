package ch.supsi.documentapplication.service.document;

import ch.supsi.documentapplication.dataaccess.IDatabaseDocumentTaskStorage;
import ch.supsi.documentapplication.model.TaskType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentTaskProgressService {

    private final IDatabaseDocumentTaskStorage documentTaskStorage;

    @Autowired
    public DocumentTaskProgressService(IDatabaseDocumentTaskStorage documentTaskStorage) {
        this.documentTaskStorage = documentTaskStorage;
    }

    public void startTask(Long documentId, TaskType type, int maxProgress) {
        documentTaskStorage.upsert(documentId, type, maxProgress);
    }

    public void updateProgress(Long documentId, TaskType type, int progress) {
        documentTaskStorage.updateProgress(documentId, type, progress);
    }

    public void completeTask(Long documentId, TaskType type) {
        documentTaskStorage.complete(documentId, type);
    }

    /**
     * Persists ERROR outside any ambient batch chunk transaction so the status
     * survives rollback of the failing step.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failTask(Long documentId, TaskType type) {
        documentTaskStorage.fail(documentId, type);
    }

    public void resumeTask(Long documentId, TaskType type) {
        documentTaskStorage.resume(documentId, type);
    }
}
