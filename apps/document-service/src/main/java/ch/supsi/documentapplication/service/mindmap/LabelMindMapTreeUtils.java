package ch.supsi.documentapplication.service.mindmap;

import ch.supsi.documentapplication.model.LabelMindMap;
import ch.supsi.documentapplication.model.LabelMindMapNode;
import ch.supsi.documentapplication.service.async.learningpipeline.Label;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class LabelMindMapTreeUtils {

    public record LabelMindMapNodeSkeleton(int nodeIndex, String labelName, Integer parentNodeIndex) {}

    private LabelMindMapTreeUtils() {}

    public static List<LabelMindMapNodeSkeleton> flattenToSkeleton(
            LabelMindMapNode root, Map<Long, String> labelNameById) {
        if (root == null) {
            return List.of();
        }
        List<LabelMindMapNodeSkeleton> skeletons = new ArrayList<>();
        flattenRecursive(root, null, labelNameById, skeletons, new int[] {0});
        return skeletons;
    }

    private static void flattenRecursive(
            LabelMindMapNode node,
            Integer parentNodeIndex,
            Map<Long, String> labelNameById,
            List<LabelMindMapNodeSkeleton> skeletons,
            int[] counter) {
        int nodeIndex = counter[0]++;
        skeletons.add(new LabelMindMapNodeSkeleton(nodeIndex, labelNameById.get(node.getLabelId()), parentNodeIndex));

        if (node.getChildren() != null) {
            for (LabelMindMapNode child : node.getChildren()) {
                flattenRecursive(child, nodeIndex, labelNameById, skeletons, counter);
            }
        }
    }

    public static void attachChildren(LabelMindMapNode parent, List<Long> childIds) {
        if (parent == null || childIds == null) {
            return;
        }
        for (Long childId : childIds) {
            parent.getChildren().add(new LabelMindMapNode(childId));
        }
    }

    public static void mergeMissingLabels(
            LabelMindMap labelMindMap, Map<Integer, List<Long>> assignmentsByParentIndex) {
        List<LabelMindMapNode> nodes = new ArrayList<>();
        collectNodes(labelMindMap.getRoot(), nodes);

        for (Map.Entry<Integer, List<Long>> entry : assignmentsByParentIndex.entrySet()) {
            if (entry.getKey() < 0 || entry.getKey() >= nodes.size()) {
                continue;
            }
            attachChildren(nodes.get(entry.getKey()), entry.getValue());
        }
    }

    public static LabelMindMap sanitize(LabelMindMap labelMindMap, List<Label> expectedLabels) {
        if (labelMindMap == null || labelMindMap.getRoot() == null) {
            return labelMindMap;
        }

        Set<Long> expectedIds = expectedLabels.stream().map(Label::id).collect(Collectors.toSet());
        Set<Long> seen = new HashSet<>();
        LabelMindMapNode sanitizedRoot = sanitizeNode(labelMindMap.getRoot(), expectedIds, seen);
        LabelMindMap result = new LabelMindMap();
        result.setRoot(sanitizedRoot);
        return result;
    }

    private static LabelMindMapNode sanitizeNode(LabelMindMapNode node, Set<Long> expectedIds, Set<Long> seen) {
        if (node == null) {
            return null;
        }

        Long labelId = node.getLabelId();
        boolean isValid = labelId != null && expectedIds.contains(labelId) && seen.add(labelId);

        List<LabelMindMapNode> sanitizedChildren = new ArrayList<>();
        if (node.getChildren() != null) {
            for (LabelMindMapNode child : node.getChildren()) {
                LabelMindMapNode sanitizedChild = sanitizeNode(child, expectedIds, seen);
                if (sanitizedChild != null) {
                    sanitizedChildren.add(sanitizedChild);
                }
            }
        }

        if (!isValid) {
            if (sanitizedChildren.size() == 1) {
                return sanitizedChildren.getFirst();
            }
            if (sanitizedChildren.isEmpty()) {
                return null;
            }
            LabelMindMapNode passthrough = new LabelMindMapNode(null);
            passthrough.setChildren(sanitizedChildren);
            return passthrough;
        }

        LabelMindMapNode sanitized = new LabelMindMapNode(labelId);
        sanitized.setChildren(sanitizedChildren);
        return sanitized;
    }

    private static void collectNodes(LabelMindMapNode root, List<LabelMindMapNode> nodes) {
        if (root == null) return;
        nodes.add(root);
        if (root.getChildren() != null) {
            for (LabelMindMapNode child : root.getChildren()) {
                collectNodes(child, nodes);
            }
        }
    }
}
