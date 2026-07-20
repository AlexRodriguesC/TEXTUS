package ch.supsi.documentapplication.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonDeserialize(using = MindMapNodeDeserializer.class)
@JsonClassDescription("A node in a rooted conceptual hierarchy tree, referencing one input topic and its child nodes.")
public class MindMapNode {

    @JsonPropertyDescription(
            "The topicId of an input topic. Must match exactly one topicId from the provided input topics, and each topicId must appear exactly once across the entire tree.")
    private Long topicId;

    @JsonPropertyDescription(
            "Child nodes of this topic in the hierarchy, representing narrower, dependent, or more specific topics. Always present; use an empty array if this node has no children.")
    private List<MindMapNode> children = new ArrayList<>();

    public MindMapNode(Long topicId) {
        this.topicId = topicId;
    }
}
