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
@JsonClassDescription("A node in a rooted conceptual hierarchy tree, referencing a page or topic and its child nodes.")
public class MindMapNode {

    @JsonPropertyDescription("Whether this node represents a page or a topic.")
    private MindMapNodeType type;

    @JsonPropertyDescription("The page id when type is PAGE.")
    private Long pageId;

    @JsonPropertyDescription("The topic id when type is TOPIC.")
    private Long topicId;

    @JsonPropertyDescription(
            "Child nodes in the hierarchy. Always present; use an empty array if this node has no children.")
    private List<MindMapNode> children = new ArrayList<>();

    public static MindMapNode page(Long pageId) {
        MindMapNode node = new MindMapNode();
        node.type = MindMapNodeType.PAGE;
        node.pageId = pageId;
        return node;
    }

    public static MindMapNode topic(Long topicId) {
        MindMapNode node = new MindMapNode();
        node.type = MindMapNodeType.TOPIC;
        node.topicId = topicId;
        return node;
    }

    public Long getEntityId() {
        return type == MindMapNodeType.PAGE ? pageId : topicId;
    }
}
