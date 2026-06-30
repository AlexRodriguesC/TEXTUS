package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.*;
import java.util.List;
import java.util.Optional;

public interface IDatabaseSpaceStorage {

    Long create(Space space);

    void update(Space space);

    Optional<Space> read(Long id);

    Optional<List<Chunk>> readChunks(Long spaceId);

    MindMap readMindMap(Long spaceId);

    List<Space> readAll(String owner);

    void updateMindMapStatus(Long spaceId, MindMapStatus status);

    Long createMindMap(Long spaceId);

    void createMindMapNodes(Long spaceId, Long mindMapId, MindMap mindMap);

    void delete(Long spaceId);
}
