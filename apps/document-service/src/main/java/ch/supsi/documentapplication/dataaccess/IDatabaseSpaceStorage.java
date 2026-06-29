package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Chunk;
import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.MindMapStatus;
import ch.supsi.documentapplication.model.Space;

import java.util.List;
import java.util.Optional;

public interface IDatabaseSpaceStorage {

    Long create(String owner, String name, String description);

    Optional<Space> read(Long id);

    Optional<List<Chunk>> readChunks(Long spaceId);

    MindMap readMindMap(Long spaceId);

    List<Space> readAll(String owner);

    void update(Long spaceId, String name, String description);

    void updateMindMapStatus(Long spaceId, MindMapStatus status);

    Long createMindMap(Long spaceId);

    void createMindMapNodes(Long spaceId, Long mindMapId, MindMap mindMap);

    void delete(Long spaceId);
}
