package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Space;
import java.util.List;

public interface IDatabaseSpaceStorage {

    Long create(String owner, String name, String description);

    Space read(Long id);

    List<Space> readAll(String owner);

    void update(Long spaceId, String name, String description);

    void delete(Long spaceId);
}
