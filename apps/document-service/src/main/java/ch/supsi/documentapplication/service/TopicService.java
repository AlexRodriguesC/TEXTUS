package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage;
import ch.supsi.documentapplication.exception.NotFoundException;
import ch.supsi.documentapplication.exception.UnauthorizedException;
import ch.supsi.documentapplication.model.Space;
import ch.supsi.documentapplication.model.Topic;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TopicService implements ITopicService {

    private final IDatabaseTopicStorage databaseTopicStorage;
    private final IDatabaseSpaceStorage databaseSpaceStorage;

    @Autowired
    public TopicService(IDatabaseTopicStorage databaseTopicStorage, IDatabaseSpaceStorage databaseSpaceStorage) {
        this.databaseTopicStorage = databaseTopicStorage;
        this.databaseSpaceStorage = databaseSpaceStorage;
    }

    @Override
    public List<Topic> getTopicsBySpace(String owner, Long spaceId) {
        var space = getSpaceOrThrow(owner, spaceId);
        return databaseTopicStorage.getBySpaceId(space.getId());
    }

    private Space getSpaceOrThrow(String username, Long spaceId) {
        var optSpace = databaseSpaceStorage.read(spaceId);
        if (optSpace.isEmpty()) {
            throw NotFoundException.spaceNotFoundException(spaceId);
        }
        var space = optSpace.get();
        return validateOwner(username, space);
    }

    private Space validateOwner(String requester, Space space) {
        if (!space.getOwner().equals(requester)) {
            throw UnauthorizedException.userNotOwnerOfSpace(requester, space.getId());
        }
        return space;
    }
}
