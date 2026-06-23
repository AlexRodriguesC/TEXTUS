package ch.supsi.documentapplication.service.clients.mindmap;

import ch.supsi.documentapplication.model.Chunk;
import ch.supsi.documentapplication.model.MindMap;
import java.util.List;

public interface MindMapClient {

    MindMap extractMindMap(List<Chunk> chunks);
}
