package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonClassDescription(
        "A rooted mind map tree built from a set of input topics, organized as a pedagogically coherent broad-to-specific hierarchy.")
public class MindMap {

    @JsonIgnore
    private Long jobId;

    @JsonPropertyDescription(
            "The single root node of the tree, representing the topic that best captures the broadest subject among all input topics.")
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
