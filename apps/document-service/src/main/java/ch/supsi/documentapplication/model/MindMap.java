package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MindMap {

    @JsonIgnore
    private Long jobId;

    private MindMapNode root;

    public Set<Long> getAllTopicIds() {
        Set<Long> ids = new HashSet<>();
        collect(root, ids);
        return ids;
    }

    public boolean isEmpty() {
        return root == null;
    }

    private void collect(MindMapNode node, Set<Long> ids) {
        if (node == null) return;
        ids.add(node.getTopicId());
        for (MindMapNode child : node.getChildren()) {
            collect(child, ids);
        }
    }
}
