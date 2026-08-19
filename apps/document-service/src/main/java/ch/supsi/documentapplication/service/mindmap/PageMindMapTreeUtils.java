package ch.supsi.documentapplication.service.mindmap;

import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.model.PageMindMap;
import ch.supsi.documentapplication.model.PageMindMapNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class PageMindMapTreeUtils {

    public record PageMindMapNodeSkeleton(int nodeIndex, String pageTitle, Integer parentNodeIndex) {}

    private PageMindMapTreeUtils() {}

    public static List<PageMindMapNodeSkeleton> flattenToSkeleton(
            PageMindMapNode root, Map<Long, String> pageTitleById) {
        if (root == null) {
            return List.of();
        }
        List<PageMindMapNodeSkeleton> skeletons = new ArrayList<>();
        flattenRecursive(root, null, pageTitleById, skeletons, new int[] {0});
        return skeletons;
    }

    private static void flattenRecursive(
            PageMindMapNode node,
            Integer parentNodeIndex,
            Map<Long, String> pageTitleById,
            List<PageMindMapNodeSkeleton> skeletons,
            int[] counter) {
        int nodeIndex = counter[0]++;
        skeletons.add(new PageMindMapNodeSkeleton(nodeIndex, pageTitleById.get(node.getPageId()), parentNodeIndex));

        if (node.getChildren() != null) {
            for (PageMindMapNode child : node.getChildren()) {
                flattenRecursive(child, nodeIndex, pageTitleById, skeletons, counter);
            }
        }
    }

    public static void attachChildren(PageMindMapNode parent, List<Long> childIds) {
        if (parent == null || childIds == null) {
            return;
        }
        for (Long childId : childIds) {
            parent.getChildren().add(new PageMindMapNode(childId));
        }
    }

    public static void mergeMissingPages(PageMindMap pageMindMap, Map<Integer, List<Long>> assignmentsByParentIndex) {
        List<PageMindMapNode> nodes = new ArrayList<>();
        collectNodes(pageMindMap.getRoot(), nodes);

        for (Map.Entry<Integer, List<Long>> entry : assignmentsByParentIndex.entrySet()) {
            if (entry.getKey() < 0 || entry.getKey() >= nodes.size()) {
                continue;
            }
            attachChildren(nodes.get(entry.getKey()), entry.getValue());
        }
    }

    public static PageMindMap sanitize(PageMindMap pageMindMap, List<Page> expectedPages) {
        if (pageMindMap == null || pageMindMap.getRoot() == null) {
            return pageMindMap;
        }

        Set<Long> expectedIds = expectedPages.stream().map(Page::getId).collect(Collectors.toSet());
        Set<Long> seen = new HashSet<>();
        PageMindMapNode sanitizedRoot = sanitizeNode(pageMindMap.getRoot(), expectedIds, seen);
        PageMindMap result = new PageMindMap();
        result.setRoot(sanitizedRoot);
        return result;
    }

    private static PageMindMapNode sanitizeNode(PageMindMapNode node, Set<Long> expectedIds, Set<Long> seen) {
        if (node == null) {
            return null;
        }

        Long pageId = node.getPageId();
        boolean isValid = pageId != null && expectedIds.contains(pageId) && seen.add(pageId);

        List<PageMindMapNode> sanitizedChildren = new ArrayList<>();
        if (node.getChildren() != null) {
            for (PageMindMapNode child : node.getChildren()) {
                PageMindMapNode sanitizedChild = sanitizeNode(child, expectedIds, seen);
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
            PageMindMapNode passthrough = new PageMindMapNode(null);
            passthrough.setChildren(sanitizedChildren);
            return passthrough;
        }

        PageMindMapNode sanitized = new PageMindMapNode(pageId);
        sanitized.setChildren(sanitizedChildren);
        return sanitized;
    }

    private static void collectNodes(PageMindMapNode root, List<PageMindMapNode> nodes) {
        if (root == null) return;
        nodes.add(root);
        if (root.getChildren() != null) {
            for (PageMindMapNode child : root.getChildren()) {
                collectNodes(child, nodes);
            }
        }
    }
}
