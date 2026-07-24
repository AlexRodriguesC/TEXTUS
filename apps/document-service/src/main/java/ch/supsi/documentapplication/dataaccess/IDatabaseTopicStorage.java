package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.learn.Topic;
import java.util.List;

public interface IDatabaseTopicStorage {

    List<Topic> getByPageId(Long pageId);

    List<Topic> getBySpaceId(Long spaceId);

    Long create(Topic topic, Long pageId);
}
