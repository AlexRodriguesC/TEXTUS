package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.MindMapStatus;
import ch.supsi.documentapplication.model.Space;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.jspecify.annotations.Nullable;

public interface ISpaceService {

    List<Space> getSpaces(String owner);

    Long create(Space space);

    void update(Space space);

    void delete(String username, Long spaceId);

    void createMindMap(String username, @NotNull Long spaceId);

    @Nullable
    MindMap getMindMap(String username, @NotNull Long spaceId);

    MindMapStatus getMindMapStatus(String username, @NotNull Long spaceId);
}
