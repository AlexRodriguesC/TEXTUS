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
        "A rooted mind map tree built from pages and topics, organized as a pedagogically coherent broad-to-specific hierarchy.")
public class MindMap {

    @JsonIgnore
    private Long id;

    @JsonIgnore
    private Long jobId;

    @JsonPropertyDescription(
            "The single root node of the tree, representing the page that best captures the broadest subject.")
    private MindMapNode root;

    public Set<Long> getAllTopicIds() {
        Set<Long> ids = new HashSet<>();
        collectTopics(root, ids);
        return ids;
    }

    public Set<Long> getAllPageIds() {
        Set<Long> ids = new HashSet<>();
        collectPages(root, ids);
        return ids;
    }

    public boolean isEmpty() {
        return root == null;
    }

    private void collectTopics(MindMapNode node, Set<Long> ids) {
        if (node == null) return;
        if (node.getType() == MindMapNodeType.TOPIC) {
            ids.add(node.getTopicId());
        }
        for (MindMapNode child : node.getChildren()) {
            collectTopics(child, ids);
        }
    }

    private void collectPages(MindMapNode node, Set<Long> ids) {
        if (node == null) return;
        if (node.getType() == MindMapNodeType.PAGE) {
            ids.add(node.getPageId());
        }
        for (MindMapNode child : node.getChildren()) {
            collectPages(child, ids);
        }
    }
}
