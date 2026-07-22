package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonClassDescription("A node in a page-based conceptual hierarchy tree.")
public class PageMindMapNode {

    @JsonPropertyDescription(
            "The pageId of an input page. Must match exactly one pageId from the provided input pages, and each pageId must appear exactly once across the entire tree.")
    private Long pageId;

    @JsonPropertyDescription(
            "Child page nodes in the hierarchy. Always present; use an empty array if this node has no children.")
    private List<PageMindMapNode> children = new ArrayList<>();

    public PageMindMapNode(Long pageId) {
        this.pageId = pageId;
    }

    public PageMindMapNode(Long pageId, List<PageMindMapNode> children) {
        this.pageId = pageId;
        this.children = children != null ? children : new ArrayList<>();
    }
}
