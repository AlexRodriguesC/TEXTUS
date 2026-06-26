package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.exception.DocumentDomainException;
import ch.supsi.documentapplication.model.Chunk;
import ch.supsi.documentapplication.model.Document;
import ch.supsi.documentapplication.model.DocumentStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Repository
@Log4j2
public class DatabaseDocumentStorage extends DatabaseStorage implements IDatabaseDocumentStorage {

    private static final String SPACE_ID = "space_id";
    private static final String DOCUMENT_ID = "document_id";
    private static final String STATUS = "status";
    private static final String DOCUMENT_STATUS = "status";

    private final JdbcClient jdbcClient;

    @Autowired
    public DatabaseDocumentStorage(JdbcTemplate jdbc, JdbcClient jdbcClient) {
        super(jdbc);
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Long create(Chunk chunk, Long spaceId, Long documentId) {
        return jdbc.execute((ConnectionCallback<Long>) connection -> {
            try (var stmt = connection.prepareStatement("SELECT documents.create_chunk(?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

                stmt.setLong(1, spaceId);
                stmt.setInt(2, chunk.getTokenCountInput());
                stmt.setInt(3, chunk.getTokenCountOutput());

                stmt.setString(4, chunk.getTitle());
                stmt.setString(5, chunk.getSummary());
                stmt.setString(6, chunk.getKey());
                stmt.setArray(7, buildArray(chunk.getKeywords(), connection));
                stmt.setArray(8, buildArray(chunk.getRelatedKeys(), connection));
                stmt.setLong(9, documentId);

                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                    throw new DocumentDomainException("documents.create_chunk returned no id");
                }
            }
        });
    }

    @Override
    public Long create(Document document, Long spaceId) {
        return jdbcClient
                .sql("SELECT documents.create_document(:space_id)")
                .param(SPACE_ID, spaceId)
                .query(Long.class)
                .single();
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
    public List<DocumentStatus> getDocumentStatuses(Long spaceId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_document_statuses(:space_id)")
                .param(SPACE_ID, spaceId)
                .query((rs, rowNum) -> DocumentStatus.valueOf(rs.getString(DOCUMENT_STATUS)))
                .list();
    }

    @Override
    public Chunk read(Long chunkId) {
        String sql = "select * from documents.read_chunk(?)";

        return jdbc.queryForObject(
                sql,
                (rs, rowNum) -> new Chunk(
                        rs.getLong("id"),
                        rs.getLong("document_id"),
                        rs.getInt("tokens_in"),
                        rs.getInt("tokens_out"),
                        rs.getString("title"),
                        rs.getString("summary"),
                        toList(rs.getArray("keywords")),
                        rs.getString("key"),
                        toList(rs.getArray("related_keys"))),
                chunkId);
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
