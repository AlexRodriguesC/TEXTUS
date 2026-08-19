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
        "A rooted page hierarchy tree built from input pages, organized as a pedagogically coherent broad-to-specific hierarchy.")
public class PageMindMap {

    @JsonPropertyDescription(
            "The single root node of the tree, representing the page that best captures the broadest subject among all input pages.")
    private PageMindMapNode root;

    public Set<Long> getAllPageIds() {
        Set<Long> ids = new HashSet<>();
        collect(root, ids);
        return ids;
    }

    public List<PageMindMapNode> getAllNodes() {
        List<PageMindMapNode> nodes = new ArrayList<>();
        collectNodes(root, nodes);
        return nodes;
    }

    private void collect(PageMindMapNode node, Set<Long> ids) {
        if (node == null) return;
        ids.add(node.getPageId());
        if (node.getChildren() != null) {
            for (PageMindMapNode child : node.getChildren()) {
                collect(child, ids);
            }
        }
    }

    private void collectNodes(PageMindMapNode node, List<PageMindMapNode> nodes) {
        if (node == null) return;
        nodes.add(node);
        if (node.getChildren() != null) {
            for (PageMindMapNode child : node.getChildren()) {
                collectNodes(child, nodes);
            }
        }
    }
}
