package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.MindMapNode;
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
public class MindMapResponseDTO {

    private List<NodeDTO> nodes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodeDTO {
        private Long chunkId;
        private Long parentChunkId;
    }

    public static MindMapResponseDTO from(MindMap mindMap) {
        List<NodeDTO> nodes = new ArrayList<>();

        if (mindMap != null && mindMap.getRoot() != null) {
            flatten(mindMap.getRoot(), null, nodes);
        }

        return MindMapResponseDTO.builder().nodes(nodes).build();
    }

    private static void flatten(MindMapNode node, Long parentChunkId, List<NodeDTO> nodes) {
        nodes.add(NodeDTO.builder()
                .chunkId(node.getTopicId())
                .parentChunkId(parentChunkId)
                .build());

        if (node.getChildren() == null) {
            return;
        }

        for (MindMapNode child : node.getChildren()) {
            flatten(child, node.getTopicId(), nodes);
        }
    }
}
