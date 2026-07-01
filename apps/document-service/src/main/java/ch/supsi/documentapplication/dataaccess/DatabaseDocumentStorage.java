package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.model.*;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
public class DatabaseDocumentStorage extends DatabaseStorage implements IDatabaseDocumentStorage {

    private static final String ID = "id";
    private static final String SPACE_ID = "space_id";
    private static final String DOCUMENT_ID = "document_id";
    private static final String STATUS = "status";
    public static final String CHUNK_ID = "chunk_id";
    public static final String TOKENS_IN = "tokens_in";
    public static final String TOKENS_OUT = "tokens_out";
    public static final String TITLE = "title";
    public static final String SUMMARY = "summary";
    public static final String KEY = "key";
    public static final String KEYWORDS = "keywords";
    public static final String RELATED_KEYS = "related_keys";
    private static final String LABELS = "labels";
    private static final String PAGE_ID = "page_id";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseDocumentStorage(JdbcTemplate jdbcTemplate, JdbcClient jdbcClient) {
        super(jdbcClient);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long create(Chunk chunk, Long spaceId, Long documentId) {
        try {
            return jdbcTemplate.execute((ConnectionCallback<Long>) connection -> {
                Array keywordsArray = buildArray(chunk.getKeywords(), connection);
                Array relatedKeysArray = buildArray(chunk.getRelatedKeys(), connection);

                return jdbcClient
                        .sql(
                                """
                        SELECT documents.create_chunk(
                            :space_id, :tokens_in, :tokens_out, :title,
                            :summary, :key, :keywords, :related_keys, :document_id
                        )
                    """)
                        .param(SPACE_ID, spaceId)
                        .param(TOKENS_IN, chunk.getTokenCountInput())
                        .param(TOKENS_OUT, chunk.getTokenCountOutput())
                        .param(TITLE, chunk.getTitle())
                        .param(SUMMARY, chunk.getSummary())
                        .param(KEY, chunk.getKey())
                        .param(KEYWORDS, keywordsArray)
                        .param(RELATED_KEYS, relatedKeysArray)
                        .param(DOCUMENT_ID, documentId)
                        .query(Long.class)
                        .optional()
                        .orElseThrow(() -> NotFoundException.spaceOrDocumentNotFoundException(spaceId, documentId));
            });
        } catch (org.springframework.dao.DataAccessException e) {
            throw NotFoundException.spaceOrDocumentNotFoundException(spaceId, documentId);
        }
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
    public Chunk read(Long chunkId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_chunk(:chunk_id)")
                .param(CHUNK_ID, chunkId)
                .query((rs, rowNum) -> new Chunk(
                        rs.getLong("id"),
                        rs.getLong("document_id"),
                        rs.getInt("tokens_in"),
                        rs.getInt("tokens_out"),
                        rs.getString("title"),
                        rs.getString("summary"),
                        toList(rs.getArray("keywords")),
                        rs.getString("key"),
                        toList(rs.getArray("related_keys"))))
                .single();
    }

    @Override
    public List<Page> getPages(Long documentId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_pages(:document_id)")
                .param(DOCUMENT_ID, documentId)
                .query((rs, rowNum) -> new Page(rs.getLong("id"), rs.getString("title"), rs.getString("summary")))
                .list();
    }

    @Override
    public List<Topic> readTopics(Long pageId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_topics(:page_id)")
                .param(PAGE_ID, pageId)
                .query((rs, rowNum) -> {
                    Array sqlLabels = rs.getArray(LABELS);
                    List<String> labels =
                            sqlLabels != null ? Arrays.asList((String[]) sqlLabels.getArray()) : List.of();
                    return new Topic(rs.getLong(ID), rs.getString(TITLE), rs.getString(SUMMARY), labels);
                })
                .list();
    }

    @Override
    public List<Topic> readDocumentTopics(Long documentId) {
        var pages = getPages(documentId);
        List<Topic> topics = new ArrayList<>();
        for (Page page : pages) {
            topics.addAll(readTopics(page.getId()));
        }

        return topics;
    }

    @Override
    public void create(Topic topic, Long pageId) {
        jdbcClient
                .sql("SELECT documents.create_topic(:page_id, :tokens_in, :tokens_out, :title, :summary, :labels)")
                .param(PAGE_ID, pageId)
                .param(TOKENS_IN, -1)
                .param(TOKENS_OUT, -1)
                .param(TITLE, topic.getTitle())
                .param(SUMMARY, topic.getSummary())
                .param(LABELS, topic.getLabels().toArray(new String[0]))
                .query(Long.class)
                .single();
    }

    private List<String> toList(java.sql.Array sqlArray) throws SQLException {
        if (sqlArray == null) {
            return List.of();
        }

        String[] values = (String[]) sqlArray.getArray();
        return values == null ? List.of() : List.of(values);
    }

    private Array buildArray(List<String> list, Connection connection) {
        var arr = list == null || list.isEmpty() ? new String[0] : list.toArray(new String[0]);
        try {
            return connection.createArrayOf("TEXT", arr);
        } catch (Exception e) {
            log.error("Error when trying to build sql array", e);
            return null;
        }
    }
}
