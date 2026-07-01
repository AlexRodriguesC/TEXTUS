package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Topic;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
public class DatabaseTopicStorage extends DatabaseStorage implements IDatabaseTopicStorage {

    private static final String ID = "id";
    private static final String SPACE_ID = "space_id";
    private static final String DOCUMENT_ID = "document_id";
    public static final String TOKENS_IN = "tokens_in";
    public static final String TOKENS_OUT = "tokens_out";
    public static final String TITLE = "title";
    public static final String SUMMARY = "summary";
    public static final String KEY = "key";
    private static final String LABELS = "labels";
    private static final String PAGE_ID = "page_id";

    @Autowired
    public DatabaseTopicStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
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

    @Override
    public List<Topic> getByPageId(Long pageId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_topics_by_page(:page_id)")
                .param(PAGE_ID, pageId)
                .query((rs, rowNum) -> mapToTopic(rs))
                .list();
    }

    @Override
    public List<Topic> getBySpaceId(Long spaceId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_topics_by_space(:space_id)")
                .param(SPACE_ID, spaceId)
                .query((rs, rowNum) -> mapToTopic(rs))
                .list();
    }

    @Override
    public List<Topic> getByDocumentId(Long documentId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_topics_by_document(:document_id)")
                .param(DOCUMENT_ID, documentId)
                .query((rs, rowNum) -> mapToTopic(rs))
                .list();
    }

    private Topic mapToTopic(ResultSet rs) throws SQLException {
        Array sqlLabels = rs.getArray(LABELS);
        List<String> labels = sqlLabels != null ? Arrays.asList((String[]) sqlLabels.getArray()) : List.of();
        return new Topic(rs.getLong(ID), rs.getString(TITLE), rs.getString(SUMMARY), labels);
    }
}
