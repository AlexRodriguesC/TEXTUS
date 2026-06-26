package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.MindMapStatus;
import ch.supsi.documentapplication.model.Space;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface ISpaceService {

    Long create(String owner, String name, String description);

    List<Space> getSpaces(String owner);

    void delete(String username, Long spaceId);

    void update(String username, Long spaceId, String name, String description);

    void createMindMap(String username, @NotNull Long spaceId);

    @Nullable
    MindMap getMindMap(String username, @NotNull Long spaceId);

    MindMapStatus getMindMapStatus(String username, @NotNull Long spaceId);
}
