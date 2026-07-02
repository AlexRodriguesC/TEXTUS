package ch.supsi.documentapplication.service.clients.mindmap;

import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.Topic;
import org.springframework.core.retry.RetryException;

import java.util.List;

public interface MindMapClient {

    MindMap extractMindMap(List<Topic> topics) throws RetryException;
}
