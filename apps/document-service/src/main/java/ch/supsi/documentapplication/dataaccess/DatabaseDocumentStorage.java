package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.model.*;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
public class DatabaseDocumentStorage extends DatabaseStorage implements IDatabaseDocumentStorage {

    private static final String ID = "id";
    private static final String SPACE_ID = "space_id";
    private static final String DOCUMENT_ID = "document_id";
    private static final String TITLE = "title";
    private static final String SUMMARY = "summary";
    public static final String KEY = "key";
    public static final String JOB_ID = "job_id";
    public static final String DOCUMENT_NAME = "document_name";

    @Autowired
    public DatabaseDocumentStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public Long create(Page page, Long spaceId, Long documentId) {
        try {
            return jdbcClient
                    .sql("SELECT documents.create_page(:space_id, :document_id, :title, :summary)")
                    .param(SPACE_ID, spaceId)
                    .param(DOCUMENT_ID, documentId)
                    .param(TITLE, page.getTitle())
                    .param(SUMMARY, page.getSummary())
                    .query(Long.class)
                    .optional()
                    .orElseThrow(() -> NotFoundException.spaceOrDocumentNotFoundException(spaceId, documentId));
        } catch (DataAccessException e) {
            throw NotFoundException.spaceOrDocumentNotFoundException(spaceId, documentId);
        }
    }

    @Override
    public Long create(Long spaceId, Long jobId, String documentName) {
        try {
            return jdbcClient
                    .sql("SELECT documents.create_document(:space_id, :job_id, :document_name)")
                    .param(SPACE_ID, spaceId)
                    .param(JOB_ID, jobId)
                    .param(DOCUMENT_NAME, documentName)
                    .query(Long.class)
                    .optional()
                    .orElseThrow(() -> NotFoundException.spaceNotFoundException(spaceId));
        } catch (DataAccessException e) {
            log.error("Database document creation failed", e);
            throw NotFoundException.spaceNotFoundException(spaceId);
        }
    }

    @Override
    public Optional<Long> findIdByJobId(Long jobId) {
        return jdbcClient
                .sql("SELECT documents.find_document_id_by_job(:job_id)")
                .param(JOB_ID, jobId)
                .query(Long.class)
                .optional();
    }

    @Override
    public List<Document> getDocuments(Long spaceId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_documents(:space_id)")
                .param(SPACE_ID, spaceId)
                .query((rs, rowNum) -> new Document(rs.getString("name"), rs.getLong(ID), rs.getLong(JOB_ID)))
                .list();
    }

    @Override
    public List<Page> getPages(Long documentId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_pages(:document_id)")
                .param(DOCUMENT_ID, documentId)
                .query((rs, rowNum) -> new Page(rs.getLong(ID), rs.getString("title"), rs.getString("summary")))
                .list();
    }

    @Override
    public List<SpacePage> getPagesBySpaceId(Long spaceId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_pages_by_space(:space_id)")
                .param(SPACE_ID, spaceId)
                .query((rs, rowNum) -> new SpacePage(
                        rs.getLong(ID), rs.getLong(DOCUMENT_ID), rs.getString("title"), rs.getString("summary")))
                .list();
    }

    @Override
    public void deleteDocument(Long spaceId, Long documentId) {
        jdbcClient
                .sql("SELECT * FROM documents.delete_document(:space_id, :document_id)")
                .param(SPACE_ID, spaceId)
                .param(DOCUMENT_ID, documentId)
                .query()
                .singleValue();
    }
}
