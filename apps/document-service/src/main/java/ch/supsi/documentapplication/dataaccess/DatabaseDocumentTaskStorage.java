package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.DocumentTask;
import ch.supsi.documentapplication.model.TaskType;
import ch.supsi.documentapplication.model.TaskVariant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class DatabaseDocumentTaskStorage extends DatabaseStorage implements IDatabaseDocumentTaskStorage {

    private static final String DOCUMENT_ID = "document_id";
    private static final String TYPE = "type";
    private static final String PROGRESS = "progress";
    private static final String MAX_PROGRESS = "max_progress";

    @Autowired
    public DatabaseDocumentTaskStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public void upsert(Long documentId, TaskType type, int maxProgress) {
        jdbcClient
                .sql("SELECT documents.upsert_document_task(:document_id, :type, :max_progress)")
                .param(DOCUMENT_ID, documentId)
                .param(TYPE, type.name())
                .param(MAX_PROGRESS, maxProgress)
                .query()
                .singleValue();
    }

    @Override
    public void updateProgress(Long documentId, TaskType type, int progress) {
        jdbcClient
                .sql("SELECT documents.update_document_task_progress(:document_id, :type, :progress)")
                .param(DOCUMENT_ID, documentId)
                .param(TYPE, type.name())
                .param(PROGRESS, progress)
                .query()
                .singleValue();
    }

    @Override
    public void complete(Long documentId, TaskType type) {
        jdbcClient
                .sql("SELECT documents.complete_document_task(:document_id, :type)")
                .param(DOCUMENT_ID, documentId)
                .param(TYPE, type.name())
                .query()
                .singleValue();
    }

    @Override
    public void fail(Long documentId, TaskType type) {
        jdbcClient
                .sql("SELECT documents.fail_document_task(:document_id, :type)")
                .param(DOCUMENT_ID, documentId)
                .param(TYPE, type.name())
                .query()
                .singleValue();
    }

    @Override
    public List<DocumentTask> getByDocumentId(Long documentId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_document_tasks(:document_id)")
                .param(DOCUMENT_ID, documentId)
                .query((rs, rowNum) -> new DocumentTask(
                        rs.getString("id"),
                        rs.getLong(DOCUMENT_ID),
                        TaskType.valueOf(rs.getString(TYPE)),
                        rs.getInt(PROGRESS),
                        rs.getInt(MAX_PROGRESS),
                        TaskVariant.valueOf(rs.getString("variant"))))
                .list();
    }
}
