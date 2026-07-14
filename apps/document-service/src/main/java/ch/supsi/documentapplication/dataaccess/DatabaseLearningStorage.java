package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Chapter;
import ch.supsi.documentapplication.model.LearningPath;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;

@Repository
@Log4j2
public class DatabaseLearningStorage extends DatabaseStorage implements IDatabaseLearningStorage {

    public static final String INDEX = "index";
    public static final String SPACE_ID = "spaceId";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";

    public DatabaseLearningStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public void create(Chapter chapter, Long spaceId) {
        jdbcClient.sql("""
            SELECT documents.create_chapter(:spaceId, :index, :title, :description, :topics)
            """)
                .param(SPACE_ID, spaceId)
                .param(INDEX, chapter.getIndex())
                .param(TITLE, chapter.getTitle())
                .param(DESCRIPTION, chapter.getDescription())
                .param("topics", chapter.getTopics().toArray(new Long[0]))
                .update();
    }

    @Override
    public LearningPath read(Long spaceId) {
        var pathRow = jdbcClient.sql("""
            SELECT * FROM documents.read_learning_path(:spaceId)
            """)
                .param(SPACE_ID, spaceId)
                .query((rs, rowNum) -> new AbstractMap.SimpleEntry<>(
                        rs.getLong("id"),
                        rs.getLong("job_id")
                ))
                .optional();

        if (pathRow.isEmpty()) {
            return null;
        }

        List<Chapter> chapters = jdbcClient.sql("""
            SELECT * FROM documents.read_chapters(:spaceId)
            """)
                .param(SPACE_ID, spaceId)
                .query((rs, rowNum) -> new Chapter(
                        rs.getLong("id"),
                        rs.getInt(INDEX),
                        rs.getString(TITLE),
                        rs.getString(DESCRIPTION),
                        Arrays.asList((Long[]) rs.getArray("topic_ids").getArray())
                ))
                .list();

        return new LearningPath(pathRow.get().getValue(), chapters);
    }

    @Override
    public void delete(Long spaceId) {
        jdbcClient.sql("""
            SELECT documents.delete_learning_path(:spaceId)
            """)
                .param(SPACE_ID, spaceId)
                .update();
    }
}
