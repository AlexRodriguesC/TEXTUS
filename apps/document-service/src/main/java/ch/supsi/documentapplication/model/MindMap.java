package ch.supsi.documentapplication.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class MindMap {

    private MindMapStatus mindMapStatus;
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
