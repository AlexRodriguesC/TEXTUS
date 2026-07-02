package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.*;
import java.util.List;
import java.util.Optional;

public interface IDatabaseSpaceStorage {

    Long create(Space space);

    void update(Space space);

    Optional<Space> read(Long id);

    MindMap readMindMap(Long spaceId);

    List<Space> readAll(String owner);

    Long createMindMap(Long spaceId, Long jobId);

    void createMindMapNodes(Long spaceId, Long mindMapId, MindMap mindMap);

    void delete(Long spaceId);
}
