package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.MindMapStatus;
import ch.supsi.documentapplication.model.Space;

import java.util.List;

public interface IDatabaseSpaceStorage {

    Long create(String owner, String name, String description);

    Space read(Long id);

    MindMap readMindMap(Long spaceId);

    List<Space> readAll(String owner);

    void update(Long spaceId, String name, String description);

    void updateMindMapStatus(Long spaceId, MindMapStatus status);

    void createMindMap(Long spaceId, MindMap mindMap);

    void delete(Long spaceId);
}
