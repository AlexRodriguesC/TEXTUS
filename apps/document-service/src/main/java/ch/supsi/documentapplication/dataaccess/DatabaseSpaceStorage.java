package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.MindMapNode;
import ch.supsi.documentapplication.model.MindMapStatus;
import ch.supsi.documentapplication.model.Space;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Log4j2
public class DatabaseSpaceStorage extends DatabaseStorage implements IDatabaseSpaceStorage {

    public static final String STATUS = "status";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String OWNER_ID = "owner_id";
    private static final String SPACE_ID = "space_id";
    public static final String MIND_MAP_ID = "mind_map_id";
    public static final String CHUNK_ID = "chunk_id";
    public static final String PARENT_ID = "parent_id";
    public static final String PARENT_CHUNK_ID = "parent_chunk_id";
    public static final String OWNER = "owner";

    public DatabaseSpaceStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public Long create(String owner, String name, String description) {
        try {
            return jdbcClient
                    .sql("SELECT documents.create_space(:owner, :name, :description)")
                    .param(OWNER, owner)
                    .param(NAME, name)
                    .param(DESCRIPTION, description)
                    .query(Long.class)
                    .optional()
                    .orElseThrow(() -> NotFoundException.userNotFound(owner));
        } catch (org.springframework.dao.DataAccessException e) {
            throw NotFoundException.userNotFound(owner);
        }
    }

    @Override
    public Optional<Space> read(Long id) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_space(:id)")
                .param(ID, id)
                .query((rs, rowNum) -> Space.builder()
                        .id(rs.getLong(ID))
                        .owner(rs.getString(OWNER_ID))
                        .name(rs.getString(NAME))
                        .description(rs.getString(DESCRIPTION))
                        .build())
                .optional();
    }

    @Override
    public MindMap readMindMap(Long spaceId) {
        MindMapStatus status;
        try {
            status = MindMapStatus.valueOf(jdbcClient
                    .sql("SELECT documents.read_mind_map_status(:space_id)")
                    .param(SPACE_ID, spaceId)
                    .query(String.class)
                    .single());
        } catch (DataAccessException e) {
            throw NotFoundException.mindMapNotFoundException(spaceId);
        }

        List<MindMapRow> rows = jdbcClient
                .sql("SELECT * FROM documents.read_mind_map(:space_id)")
                .param(SPACE_ID, spaceId)
                .query((rs, rowNum) -> new MindMapRow(rs.getLong(CHUNK_ID), rs.getObject(PARENT_CHUNK_ID, Long.class)))
                .list();

        MindMap mindMap = new MindMap();
        mindMap.setMindMapStatus(status);

        if (rows.isEmpty()) {
            mindMap.setRoot(null);
            return mindMap;
        }

        Map<Long, MindMapNode> nodesByChunkId = new HashMap<>();
        MindMapNode root = null;

        for (MindMapRow row : rows) {
            nodesByChunkId.computeIfAbsent(row.chunkId(), MindMapNode::new);
        }

        for (MindMapRow row : rows) {
            MindMapNode current = nodesByChunkId.get(row.chunkId());

            if (row.parentChunkId() == null) {
                root = current;
            } else {
                MindMapNode parent = nodesByChunkId.get(row.parentChunkId());
                parent.getChildren().add(current);
            }
        }

        mindMap.setRoot(root);
        return mindMap;
    }

    private record MindMapRow(Long chunkId, Long parentChunkId) {}

    @Override
    public List<Space> readAll(String owner) {
        return jdbcClient
                .sql("SELECT * FROM documents.read_spaces(:owner)")
                .param(OWNER, owner)
                .query((rs, rowNum) -> Space.builder()
                        .id(rs.getLong(ID))
                        .owner(rs.getString(OWNER_ID))
                        .name(rs.getString(NAME))
                        .description(rs.getString(DESCRIPTION))
                        .build())
                .list();
    }

    @Override
    public void update(Long spaceId, String name, String description) {
        jdbcClient
                .sql("SELECT documents.update_space(:space_id, :name, :description)")
                .param(SPACE_ID, spaceId)
                .param(NAME, name)
                .param(DESCRIPTION, description)
                .query()
                .singleValue();
    }

    @Override
    public void updateMindMapStatus(Long spaceId, MindMapStatus status) {
        jdbcClient
                .sql("SELECT documents.update_mind_map_status(:space_id, CAST(:status AS documents.mind_map_status))")
                .param(SPACE_ID, spaceId)
                .param(STATUS, status.name())
                .query()
                .singleValue();
    }

    @Override
    public void createMindMap(Long spaceId, MindMap mindMap) {
        Long mindMapId = jdbcClient
                .sql("SELECT documents.create_mind_map(:space_id)")
                .param(SPACE_ID, spaceId)
                .query(Long.class)
                .single();

        MindMapNode root = mindMap.getRoot();
        if (root == null) {
            return;
        }

        saveMindMapNodeRecursive(mindMapId, root, null);
    }

    private void saveMindMapNodeRecursive(Long mindMapId, MindMapNode node, Long parentNodeId) {
        Long nodeId = jdbcClient
                .sql("SELECT documents.create_mind_map_node(:mind_map_id, :chunk_id, :parent_id)")
                .param(MIND_MAP_ID, mindMapId)
                .param(CHUNK_ID, node.getChunkId())
                .param(PARENT_ID, parentNodeId)
                .query(Long.class)
                .single();

        for (MindMapNode child : node.getChildren()) {
            saveMindMapNodeRecursive(mindMapId, child, nodeId);
        }
    }

    @Override
    public void delete(Long spaceId) {
        jdbcClient
                .sql("SELECT documents.delete_space(:space_id)")
                .param(SPACE_ID, spaceId)
                .query()
                .singleValue();
    }
}
