package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.model.*;
import java.util.List;
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
    private static final String STATUS = "status";
    public static final String TOKENS_IN = "tokens_in";
    public static final String TOKENS_OUT = "tokens_out";
    public static final String TITLE = "title";
    public static final String SUMMARY = "summary";
    public static final String KEY = "key";

    @Autowired
    public DatabaseDocumentStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public Long create(Page page, Long spaceId, Long documentId) {
        try {
            return jdbcClient
                    .sql(
                            "SELECT documents.create_page(:space_id, :document_id, :tokens_in, :tokens_out, :title, :summary)")
                    .param(SPACE_ID, spaceId)
                    .param(DOCUMENT_ID, documentId)
                    .param(TOKENS_IN, -1)
                    .param(TOKENS_OUT, -1)
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
    public Long create(Long spaceId) {
        try {
            return jdbcClient
                    .sql("SELECT documents.create_document(:space_id)")
                    .param(SPACE_ID, spaceId)
                    .query(Long.class)
                    .optional()
                    .orElseThrow(() -> NotFoundException.spaceNotFoundException(spaceId));
        } catch (org.springframework.dao.DataAccessException e) {
            throw NotFoundException.spaceNotFoundException(spaceId);
        }
    }

    @Override
    public void updateDocumentStatus(Long documentId, DocumentStatus status) {
        jdbcClient
                .sql("SELECT documents.update_document_status(:document_id, :status::documents.document_status)")
                .param(DOCUMENT_ID, documentId)
                .param(STATUS, status.name())
                .query()
                .singleValue();
    }

    @Override
    public List<Document> getDocuments(Long spaceId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_documents(:space_id)")
                .param(SPACE_ID, spaceId)
                .query((rs, rowNum) -> new Document(rs.getLong(ID), DocumentStatus.valueOf(rs.getString(STATUS))))
                .list();
    }

    @Override
    public List<Page> getPages(Long documentId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_pages(:document_id)")
                .param(DOCUMENT_ID, documentId)
                .query((rs, rowNum) -> new Page(rs.getLong("id"), rs.getString("title"), rs.getString("summary")))
                .list();
    }
}
