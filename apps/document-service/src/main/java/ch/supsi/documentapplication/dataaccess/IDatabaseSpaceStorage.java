package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Space;

public interface IDatabaseSpaceStorage {

    Long create(String name, String description);

    Space read(Long id);

    void update(String name, String description);

    void delete(Long id);
}
