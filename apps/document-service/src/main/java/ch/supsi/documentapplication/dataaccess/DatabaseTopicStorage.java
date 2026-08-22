package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import ch.supsi.documentapplication.model.learn.Topic;
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
    private static final String TITLE = "title";
    private static final String SUMMARY = "summary";
    public static final String KEY = "key";
    private static final String LABELS = "labels";
    private static final String EXERCISE_TYPES = "exercise_types";
    private static final String PAGE_ID = "page_id";
    private static final String PAGE_NUMBER = "page_number";
    private static final String TYPES = "types";

    @Autowired
    public DatabaseTopicStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public Long create(Topic topic, Long pageId) {
        List<ExerciseType> exerciseTypes = topic.getExerciseTypes();
        if (exerciseTypes == null || exerciseTypes.isEmpty()) {
            exerciseTypes = List.of(ExerciseType.QUESTION);
        }
        return jdbcClient
                .sql("SELECT documents.create_topic(:page_id, :title, :summary, :labels, :types)")
                .param(PAGE_ID, pageId)
                .param(TITLE, topic.getTitle())
                .param(SUMMARY, topic.getSummary())
                .param(LABELS, topic.getLabels().toArray(new String[0]))
                .param(TYPES, exerciseTypes.stream().map(Enum::name).toArray(String[]::new))
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
    public List<Topic> getByIds(Long spaceId, List<Long> topicIds) {
        if (topicIds == null || topicIds.isEmpty()) {
            return List.of();
        }
        return jdbcClient
                .sql("SELECT * FROM documents.read_topics_by_ids(:space_id, :topic_ids)")
                .param(SPACE_ID, spaceId)
                .param("topic_ids", topicIds.toArray(new Long[0]))
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

    @Override
    public List<Topic> findRandomTopicsBySpace(Long spaceId, int limit) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_random_topics_by_space(:space_id, :limit)")
                .param(SPACE_ID, spaceId)
                .param("limit", limit)
                .query((rs, rowNum) -> mapToTopic(rs))
                .list();
    }

    private Topic mapToTopic(ResultSet rs) throws SQLException {
        Array sqlLabels = rs.getArray(LABELS);
        List<String> labels = sqlLabels != null ? Arrays.asList((String[]) sqlLabels.getArray()) : List.of();
        List<ExerciseType> exerciseTypes = mapExerciseTypes(rs.getArray(EXERCISE_TYPES));
        return new Topic(
                rs.getLong(ID),
                rs.getString("src_file_name"),
                rs.getLong(PAGE_ID),
                rs.getInt(PAGE_NUMBER),
                rs.getString(TITLE),
                rs.getString(SUMMARY),
                labels,
                exerciseTypes);
    }

    private static List<ExerciseType> mapExerciseTypes(Array sqlTypes) throws SQLException {
        if (sqlTypes == null) {
            return List.of(ExerciseType.QUESTION);
        }
        String[] values = (String[]) sqlTypes.getArray();
        if (values == null || values.length == 0) {
            return List.of(ExerciseType.QUESTION);
        }
        return Arrays.stream(values).map(ExerciseType::valueOf).toList();
    }
}
