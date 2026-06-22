package ch.supsi.documentapplication.service.clients.mindmap;

import ch.supsi.documentapplication.model.Chunk;
import ch.supsi.documentapplication.model.MindMapNode;

import java.util.List;

public interface MindMapClient {

    MindMapNode extractMindMap(List<Chunk> chunks);

}
