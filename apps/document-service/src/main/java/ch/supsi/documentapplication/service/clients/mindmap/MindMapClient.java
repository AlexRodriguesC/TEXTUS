package ch.supsi.documentapplication.service.clients.mindmap;

import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.Topic;
import java.util.List;

public interface MindMapClient {

    MindMap extractMindMap(List<Topic> topics);
}
