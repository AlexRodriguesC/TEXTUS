package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.MindMapNode;
import ch.supsi.documentapplication.model.MindMapNodeType;
import ch.supsi.documentapplication.model.MindMapStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Valid
public class MindMapResponseDTO {

    @NotNull
    @Schema(description = "Unique identifier of the mind map", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "Current status of the mind map generation", example = "DONE")
    private MindMapStatus status;

    private List<NodeDTO> nodes;

    public enum NodeType {
        PAGE,
        TOPIC
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeDTO {
        @Schema(description = "Whether this node represents a page or a topic")
        private NodeType nodeType;

        @Schema(description = "The page id or topic id depending on nodeType")
        private Long entityId;

        @Schema(description = "The node type of the parent, null for the root node")
        private NodeType parentNodeType;

        @Schema(description = "The page id or topic id of the parent, null for the root node")
        private Long parentEntityId;
    }

    public static MindMapResponseDTO from(MindMap mindMap, MindMapStatus status) {
        List<NodeDTO> nodes = new ArrayList<>();

        if (mindMap != null && mindMap.getRoot() != null) {
            flatten(mindMap.getRoot(), null, null, nodes);
        }

        return MindMapResponseDTO.builder()
                .id(mindMap != null ? mindMap.getId() : null)
                .status(status)
                .nodes(nodes)
                .build();
    }

    private static void flatten(
            MindMapNode node, NodeType parentNodeType, Long parentEntityId, List<NodeDTO> nodes) {
        NodeType nodeType = toDtoType(node.getType());
        nodes.add(NodeDTO.builder()
                .nodeType(nodeType)
                .entityId(node.getEntityId())
                .parentNodeType(parentNodeType)
                .parentEntityId(parentEntityId)
                .build());

        if (node.getChildren() == null) {
            return;
        }

        for (MindMapNode child : node.getChildren()) {
            flatten(child, nodeType, node.getEntityId(), nodes);
        }
    }

    private static NodeType toDtoType(MindMapNodeType type) {
        return type == MindMapNodeType.PAGE ? NodeType.PAGE : NodeType.TOPIC;
    }
}
