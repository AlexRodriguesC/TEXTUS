package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.model.Topic;
import java.util.List;

public interface ITopicService {

    List<Topic> getTopicsBySpace(String owner, Long spaceId);
}
