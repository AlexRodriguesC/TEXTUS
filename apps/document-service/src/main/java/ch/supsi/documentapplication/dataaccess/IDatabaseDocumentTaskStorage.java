package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.DocumentTask;
import ch.supsi.documentapplication.model.TaskType;
import java.util.List;

public interface IDatabaseDocumentTaskStorage {

    void upsert(Long documentId, TaskType type, int maxProgress);

    void updateProgress(Long documentId, TaskType type, int progress);

    void complete(Long documentId, TaskType type);

    void fail(Long documentId, TaskType type);

    List<DocumentTask> getByDocumentId(Long documentId);
}
