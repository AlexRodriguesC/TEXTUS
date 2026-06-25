package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.model.Space;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpaceService implements ISpaceService {

    private final IDatabaseSpaceStorage spaceStorage;

    @Autowired
    public SpaceService(IDatabaseSpaceStorage spaceStorage) {
        this.spaceStorage = spaceStorage;
    }

    @Override
    public Long create(String owner, String name, String description) {
        return spaceStorage.create(owner, name, description);
    }

    @Override
    public List<Space> getSpaces(String owner) {
        return spaceStorage.readAll(owner);
    }

    @Override
    public void delete(String username, Long spaceId) {
        var spaces = spaceStorage.readAll(username);
        boolean found = spaces.stream().anyMatch(space -> Objects.equals(space.getId(), spaceId));
        if (!found) {
            return;
        }
        spaceStorage.delete(spaceId);
    }

    @Override
    public void update(String username, Long spaceId, String name, String description) {
        var spaces = spaceStorage.readAll(username);
        boolean found = spaces.stream().anyMatch(space -> Objects.equals(space.getId(), spaceId));
        if (!found) {
            return;
        }
        spaceStorage.update(spaceId, name, description);
    }
}
