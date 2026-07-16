package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage;
import ch.supsi.documentapplication.model.learn.Topic;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TopicService extends ch.supsi.documentapplication.service.Service implements ITopicService {

    private final IDatabaseTopicStorage databaseTopicStorage;

    @Autowired
    public TopicService(IDatabaseTopicStorage databaseTopicStorage, IDatabaseSpaceStorage databaseSpaceStorage) {
        super(databaseSpaceStorage);
        this.databaseTopicStorage = databaseTopicStorage;
    }

    @Override
    public List<Topic> getTopicsBySpace(String owner, Long spaceId) {
        var space = getSpaceOrThrow(owner, spaceId);
        return databaseTopicStorage.getBySpaceId(space.getId());
    }
}
