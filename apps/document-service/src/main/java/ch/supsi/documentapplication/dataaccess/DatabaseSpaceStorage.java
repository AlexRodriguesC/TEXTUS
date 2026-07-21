package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.exception.UnauthorizedException;
import ch.supsi.documentapplication.model.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
public class DatabaseSpaceStorage extends DatabaseStorage implements IDatabaseSpaceStorage {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String ICON = "icon";
    private static final String DESCRIPTION = "description";
    private static final String OWNER_ID = "owner_id";
    private static final String SPACE_ID = "space_id";
    public static final String MIND_MAP_ID = "mind_map_id";
    public static final String PARENT_ID = "parent_id";
    public static final String PARENT_TOPIC_ID = "parent_topic_id";
    public static final String OWNER = "owner";
    private static final String TOPIC_ID = "topic_id";
    public static final String JOB_ID = "job_id";

    public DatabaseSpaceStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public Long create(Space space) {
        try {
            return jdbcClient
                    .sql("SELECT documents.create_space(:owner, :icon, :name, :description)")
                    .param(OWNER, space.getOwner())
                    .param(ICON, space.getIcon())
                    .param(NAME, space.getName())
                    .param(DESCRIPTION, space.getDescription())
                    .query(Long.class)
                    .optional()
                    .orElseThrow(() -> NotFoundException.userNotFound(space.getOwner()));
        } catch (org.springframework.dao.DataAccessException e) {
            throw NotFoundException.userNotFound(space.getOwner());
        }
    }

    @Override
    public void update(Space space) {
        jdbcClient
                .sql("SELECT documents.update_space(:space_id, :icon, :name, :description)")
                .param(SPACE_ID, space.getId())
                .param(ICON, space.getIcon())
                .param(NAME, space.getName())
                .param(DESCRIPTION, space.getDescription())
                .query()
                .singleValue();
    }

    @Override
    public Optional<Space> read(Long id) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_space(:id)")
                .param(ID, id)
                .query((rs, rowNum) -> mapToSpace(rs))
                .optional();
    }

    @Override
    public MindMap readMindMap(Long spaceId) {
        List<MindMapRow> rows = jdbcClient
                .sql("SELECT * FROM documents.read_mind_map(:space_id)")
                .param(SPACE_ID, spaceId)
                .query((rs, rowNum) -> new MindMapRow(
                        rs.getLong(JOB_ID),
                        rs.getObject(TOPIC_ID, Long.class),
                        rs.getObject(PARENT_TOPIC_ID, Long.class)))
                .list();

        if (rows.isEmpty()) {
            return null;
        }

        MindMap mindMap = new MindMap();
        mindMap.setJobId(rows.getFirst().jobId());

        List<MindMapRow> nodeRows =
                rows.stream().filter(row -> row.topicId() != null).toList();
        if (nodeRows.isEmpty()) {
            mindMap.setRoot(null);
            return mindMap;
        }

        Map<Long, MindMapNode> nodesByChunkId = new HashMap<>();
        MindMapNode root = null;

        for (MindMapRow row : nodeRows) {
            nodesByChunkId.computeIfAbsent(row.topicId(), MindMapNode::new);
        }

        for (MindMapRow row : nodeRows) {
            MindMapNode current = nodesByChunkId.get(row.topicId());

            if (row.parentTopicId() == null) {
                root = current;
            } else {
                MindMapNode parent = nodesByChunkId.get(row.parentTopicId());
                parent.getChildren().add(current);
            }
        }

        mindMap.setRoot(root);
        return mindMap;
    }

    private record MindMapRow(Long jobId, Long topicId, Long parentTopicId) {}

    @Override
    public List<Space> readAll(String owner) {
        try {
            return jdbcClient
                    .sql("SELECT * FROM documents.read_spaces(:owner)")
                    .param(OWNER, owner)
                    .query((rs, rowNum) -> mapToSpace(rs))
                    .list();
        } catch (DataAccessException e) {
            throw UnauthorizedException.userNotAuthorizedToReadSpaces(owner);
        }
    }

    @Override
    public Long createMindMap(Long spaceId, Long jobId) {
        try {
            return jdbcClient
                    .sql("SELECT documents.create_mind_map(:space_id, :job_id)")
                    .param(SPACE_ID, spaceId)
                    .param(JOB_ID, jobId)
                    .query(Long.class)
                    .single();
        } catch (DataAccessException e) {
            throw NotFoundException.spaceNotFoundException(spaceId);
        }
    }

    @Override
    public void createMindMapNodes(Long spaceId, Long mindMapId, MindMap mindMap) {
        MindMapNode root = mindMap.getRoot();
        if (root == null) {
            return;
        }

        saveMindMapNodeRecursive(mindMapId, root, null);
    }

    private void saveMindMapNodeRecursive(Long mindMapId, MindMapNode node, Long parentNodeId) {
        Long nodeId;

        try {
            nodeId = jdbcClient
                    .sql("SELECT documents.create_mind_map_node(:mind_map_id, :topic_id, :parent_id)")
                    .param(MIND_MAP_ID, mindMapId)
                    .param(TOPIC_ID, node.getTopicId())
                    .param(PARENT_ID, parentNodeId)
                    .query(Long.class)
                    .single();
        } catch (DataAccessException e) {
            throw NotFoundException.mindMapNotFoundException(mindMapId);
        }

        for (MindMapNode child : node.getChildren()) {
            saveMindMapNodeRecursive(mindMapId, child, nodeId);
        }
    }

    @Override
    public void deleteMindMap(Long spaceId) {
        jdbcClient
                .sql("SELECT documents.delete_mind_map(:space_id)")
                .param(SPACE_ID, spaceId)
                .query()
                .singleValue();
    }

    @Override
    public void delete(Long spaceId) {
        jdbcClient
                .sql("SELECT documents.delete_space(:space_id)")
                .param(SPACE_ID, spaceId)
                .query()
                .singleValue();
    }

    private Space mapToSpace(ResultSet rs) throws SQLException {
        return new Space(
                rs.getLong(ID),
                rs.getString(OWNER_ID),
                rs.getString(ICON),
                rs.getString(NAME),
                rs.getString(DESCRIPTION));
    }
}
