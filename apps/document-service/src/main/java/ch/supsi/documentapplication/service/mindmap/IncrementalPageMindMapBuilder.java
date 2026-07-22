package ch.supsi.documentapplication.service.mindmap;

import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.model.PageMindMap;
import ch.supsi.documentapplication.model.PageMindMapNode;
import ch.supsi.documentapplication.model.PageTopicGroup;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class IncrementalPageMindMapBuilder {

    public PageMindMap build(List<Page> allPages, List<PageTopicGroup> groups, MindMapExtractionSteps steps) {
        if (allPages.isEmpty()) {
            return new PageMindMap();
        }

        if (allPages.size() == 1) {
            PageMindMap single = new PageMindMap();
            single.setRoot(new PageMindMapNode(allPages.getFirst().getId()));
            return single;
        }

        Set<Long> allPageIds = allPages.stream().map(Page::getId).collect(Collectors.toSet());
        Long rootId = steps.selectRoot(allPages, groups);
        if (rootId == null || !allPageIds.contains(rootId)) {
            return new PageMindMap();
        }

        Map<Long, Page> pageById =
                allPages.stream().collect(Collectors.toMap(Page::getId, page -> page, (left, right) -> left));
        Set<Long> remaining = new LinkedHashSet<>(allPageIds);
        remaining.remove(rootId);

        PageMindMapNode root = new PageMindMapNode(rootId);
        Queue<PageMindMapNode> queue = new ArrayDeque<>();
        queue.add(root);

        while (!remaining.isEmpty() && !queue.isEmpty()) {
            PageMindMapNode parent = queue.poll();
            assignDirectChildren(parent, remaining, pageById, groups, steps, queue);
        }

        PageMindMap pageMindMap = new PageMindMap();
        pageMindMap.setRoot(root);
        return pageMindMap;
    }

    private void assignDirectChildren(
            PageMindMapNode parent,
            Set<Long> remaining,
            Map<Long, Page> pageById,
            List<PageTopicGroup> groups,
            MindMapExtractionSteps steps,
            Queue<PageMindMapNode> queue) {
        if (remaining.isEmpty()) {
            return;
        }

        List<Page> remainingPages = remaining.stream().map(pageById::get).toList();
        List<Long> childIds = steps.selectChildren(parent, remainingPages, groups);
        List<Long> validChildIds = filterValidChildIds(childIds, remaining);

        for (Long childId : validChildIds) {
            PageMindMapNode child = new PageMindMapNode(childId);
            parent.getChildren().add(child);
            remaining.remove(childId);
            queue.add(child);
        }
    }

    private List<Long> filterValidChildIds(List<Long> childIds, Set<Long> remaining) {
        if (childIds == null || childIds.isEmpty()) {
            return List.of();
        }

        Set<Long> seen = new HashSet<>();
        List<Long> valid = new ArrayList<>();
        for (Long childId : childIds) {
            if (childId != null && remaining.contains(childId) && seen.add(childId)) {
                valid.add(childId);
            }
        }
        return valid;
    }
}
