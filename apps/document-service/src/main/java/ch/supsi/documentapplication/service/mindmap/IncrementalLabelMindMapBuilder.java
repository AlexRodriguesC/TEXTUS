package ch.supsi.documentapplication.service.mindmap;

import ch.supsi.documentapplication.model.LabelMindMap;
import ch.supsi.documentapplication.model.LabelMindMapNode;
import ch.supsi.documentapplication.model.LabelTopicGroup;
import ch.supsi.documentapplication.service.async.learningpipeline.Label;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class IncrementalLabelMindMapBuilder {

    public LabelMindMap build(List<Label> allLabels, List<LabelTopicGroup> groups, MindMapExtractionSteps steps) {
        if (allLabels.isEmpty()) {
            return new LabelMindMap();
        }

        if (allLabels.size() == 1) {
            LabelMindMap single = new LabelMindMap();
            single.setRoot(new LabelMindMapNode(allLabels.getFirst().id()));
            return single;
        }

        Set<Long> allLabelIds = allLabels.stream().map(Label::id).collect(Collectors.toSet());
        Long rootId = steps.selectRoot(allLabels, groups);
        if (rootId == null || !allLabelIds.contains(rootId)) {
            return new LabelMindMap();
        }

        Map<Long, Label> labelById =
                allLabels.stream().collect(Collectors.toMap(Label::id, label -> label, (left, right) -> left));
        Set<Long> remaining = new LinkedHashSet<>(allLabelIds);
        remaining.remove(rootId);

        LabelMindMapNode root = new LabelMindMapNode(rootId);
        Queue<LabelMindMapNode> queue = new ArrayDeque<>();
        queue.add(root);

        while (!remaining.isEmpty() && !queue.isEmpty()) {
            LabelMindMapNode parent = queue.poll();
            assignDirectChildren(parent, remaining, labelById, groups, steps, queue);
        }

        LabelMindMap labelMindMap = new LabelMindMap();
        labelMindMap.setRoot(root);
        return labelMindMap;
    }

    private void assignDirectChildren(
            LabelMindMapNode parent,
            Set<Long> remaining,
            Map<Long, Label> labelById,
            List<LabelTopicGroup> groups,
            MindMapExtractionSteps steps,
            Queue<LabelMindMapNode> queue) {
        if (remaining.isEmpty()) {
            return;
        }

        List<Label> remainingLabels = remaining.stream().map(labelById::get).toList();
        List<Long> childIds = steps.selectChildren(parent, remainingLabels, groups);
        List<Long> validChildIds = filterValidChildIds(childIds, remaining);

        for (Long childId : validChildIds) {
            LabelMindMapNode child = new LabelMindMapNode(childId);
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
