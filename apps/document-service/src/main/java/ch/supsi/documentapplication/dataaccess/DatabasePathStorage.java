package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.PathSettings;
import ch.supsi.documentapplication.model.learn.Chapter;
import ch.supsi.documentapplication.model.learn.LearningPath;
import ch.supsi.documentapplication.model.learn.Node;
import ch.supsi.documentapplication.model.learn.NodeType;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
public class DatabasePathStorage extends DatabaseStorage implements IDatabasePathStorage {

    public static final String INDEX = "index";
    public static final String SPACE_ID = "spaceId";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";

    public DatabasePathStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public void create(Long pathId, Chapter chapter, Long spaceId) {
        Long chapterId = jdbcClient
                .sql(
                        """
            SELECT documents.create_chapter(:spaceId, :pathId, :index, :title, :description)
            """)
                .param(SPACE_ID, spaceId)
                .param("pathId", pathId)
                .param(INDEX, chapter.getIndex())
                .param(TITLE, chapter.getTitle())
                .param(DESCRIPTION, chapter.getDescription())
                .query(Long.class)
                .single();

        var nodes = chapter.getNodes();

        for (Node node : nodes) {
            jdbcClient
                    .sql("SELECT documents.create_node(:chapter_id, :index, :type, :topics)")
                    .param("chapter_id", chapterId)
                    .param("index", node.getIndex())
                    .param("type", node.getType().name().toLowerCase())
                    .param("topics", node.getTopics().toArray(new Long[0]))
                    .query(Long.class)
                    .single();
        }
    }

    @Override
    public Long createLearningPath(Long spaceId, Long executionId) {
        return jdbcClient
                .sql("""
            SELECT documents.create_learning_path(:spaceId, :jobId)
            """)
                .param(SPACE_ID, spaceId)
                .param("jobId", executionId)
                .query(Long.class)
                .single();
    }

    @Override
    public LearningPath read(Long spaceId) {
        var pathRow = jdbcClient
                .sql("""
                SELECT * FROM documents.read_learning_path(:spaceId)
                """)
                .param(SPACE_ID, spaceId)
                .query((rs, rowNum) -> {
                    Long jobId = rs.getLong("job_id");
                    PathSettings settings = new PathSettings(
                            rs.getInt("question_percentage"),
                            rs.getInt("list_percentage"),
                            rs.getInt("group_percentage"),
                            rs.getInt("pair_percentage"));
                    return new AbstractMap.SimpleEntry<>(jobId, settings);
                })
                .optional();

        if (pathRow.isEmpty()) {
            return null;
        }

        Long jobId = pathRow.get().getKey();
        PathSettings settings = pathRow.get().getValue();

        List<Chapter> chapters = jdbcClient
                .sql("""
                SELECT * FROM documents.read_chapters(:spaceId)
                """)
                .param(SPACE_ID, spaceId)
                .query((rs, rowNum) -> new Chapter(
                        rs.getLong("id"), rs.getInt(INDEX), rs.getString(TITLE), rs.getString(DESCRIPTION), null))
                .list();

        for (Chapter chapter : chapters) {
            List<Node> nodes = getNodesByChapterId(chapter.getId());
            chapter.setNodes(nodes);
        }

        return new LearningPath(jobId, settings, chapters);
    }

    private List<Node> getNodesByChapterId(Long chapterId) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_nodes(:chapter_id)")
                .param("chapter_id", chapterId)
                .query((rs, rowNum) -> {
                    Long id = rs.getLong("id");
                    Integer index = rs.getInt("index");
                    String typeStr = rs.getString("type");
                    NodeType type = NodeType.valueOf(typeStr.toUpperCase());

                    java.sql.Array topicsArray = rs.getArray("topics");
                    List<Long> topics;
                    if (topicsArray != null) {
                        Long[] arr = (Long[]) topicsArray.getArray();
                        topics = Arrays.asList(arr);
                    } else {
                        topics = List.of();
                    }

                    return new Node(id, type, index, topics);
                })
                .list();
    }

    @Override
    public void delete(Long spaceId) {
        jdbcClient
                .sql("""
            SELECT documents.delete_learning_path(:spaceId)
            """)
                .param(SPACE_ID, spaceId)
                .query()
                .singleValue();
    }

    @Override
    public void updateSettings(Long id, PathSettings settings) {
        jdbcClient
                .sql(
                        "SELECT documents.update_path_settings(:spaceId, :questionPercentage, :listPercentage, :groupPercentage, :pairPercentage)")
                .param(SPACE_ID, id)
                .param("questionPercentage", settings.getQuestionPercentage())
                .param("listPercentage", settings.getListPercentage())
                .param("groupPercentage", settings.getGroupPercentage())
                .param("pairPercentage", settings.getPairPercentage())
                .query()
                .singleValue();
    }
}
