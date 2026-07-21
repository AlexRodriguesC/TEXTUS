package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonClassDescription(
        "A rooted label hierarchy tree built from distinct input labels, organized as a pedagogically coherent broad-to-specific hierarchy.")
public class LabelMindMap {

    @JsonPropertyDescription(
            "The single root node of the tree, representing the label that best captures the broadest subject among all input labels.")
    private LabelMindMapNode root;

    public Set<Long> getAllLabelIds() {
        Set<Long> ids = new HashSet<>();
        collect(root, ids);
        return ids;
    }

    public List<LabelMindMapNode> getAllNodes() {
        List<LabelMindMapNode> nodes = new ArrayList<>();
        collectNodes(root, nodes);
        return nodes;
    }

    private void collect(LabelMindMapNode node, Set<Long> ids) {
        if (node == null) return;
        ids.add(node.getLabelId());
        if (node.getChildren() != null) {
            for (LabelMindMapNode child : node.getChildren()) {
                collect(child, ids);
            }
        }
    }

    private void collectNodes(LabelMindMapNode node, List<LabelMindMapNode> nodes) {
        if (node == null) return;
        nodes.add(node);
        if (node.getChildren() != null) {
            for (LabelMindMapNode child : node.getChildren()) {
                collectNodes(child, nodes);
            }
        }
    }
}
