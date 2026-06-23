package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
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
    public Long create(String name, String description) {
        return spaceStorage.create(name, description);
    }
}
