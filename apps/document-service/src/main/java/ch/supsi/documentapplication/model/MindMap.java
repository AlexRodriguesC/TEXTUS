package ch.supsi.documentapplication.model;

import java.util.HashSet;
import java.util.Set;

public class MindMap {

    private MindMapNode root;

    public Set<Long> getAllChunkIds() {
        Set<Long> ids = new HashSet<>();
        collect(root, ids);
        return ids;
    }

    private void collect(MindMapNode node, Set<Long> ids) {
        if (node == null) return;
        ids.add(node.getChunkId());
        for (MindMapNode child : node.getChildren()) {
            collect(child, ids);
        }
    }
}
