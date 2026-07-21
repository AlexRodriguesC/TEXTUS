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
@JsonClassDescription("A node in a label-based conceptual hierarchy tree.")
public class LabelMindMapNode {

    @JsonPropertyDescription(
            "The labelId of an input label. Must match exactly one labelId from the provided input labels, and each labelId must appear exactly once across the entire tree.")
    private Long labelId;

    @JsonPropertyDescription(
            "Child label nodes in the hierarchy. Always present; use an empty array if this node has no children.")
    private List<LabelMindMapNode> children = new ArrayList<>();

    public LabelMindMapNode(Long labelId) {
        this.labelId = labelId;
    }

    public LabelMindMapNode(Long labelId, List<LabelMindMapNode> children) {
        this.labelId = labelId;
        this.children = children != null ? children : new ArrayList<>();
    }
}
