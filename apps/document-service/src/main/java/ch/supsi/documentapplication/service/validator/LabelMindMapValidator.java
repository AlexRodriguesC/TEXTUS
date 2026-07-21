package ch.supsi.documentapplication.service.validator;

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException;
import ch.supsi.documentapplication.model.LabelMindMap;
import ch.supsi.documentapplication.model.LabelMindMapNode;
import ch.supsi.documentapplication.service.async.learningpipeline.Label;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LabelMindMapValidator implements FeedbackAwareValidator<LabelMindMap, List<Label>, LabelMindMapFeedback> {

    @Override
    public void validate(LabelMindMap actual, List<Label> expected) {
        if (actual == null || expected == null) {
            throw AIOutputMappingFailedException.create("label-mind-map", "label mind map or labels cannot be null");
        }

        LabelMindMapFeedback feedback = extractFeedback(actual, expected);
        if (!feedback.isValid()) {
            throw AIOutputMappingFailedException.create(
                    "label-mind-map", "label mind map does not match expected labels");
        }
    }

    @Override
    public LabelMindMapFeedback extractFeedback(LabelMindMap actual, List<Label> expected) {
        if (actual == null || expected == null) {
            throw AIOutputMappingFailedException.create("label-mind-map", "label mind map or labels cannot be null");
        }

        List<Long> actualIds = collectLabelIds(actual);
        List<Long> expectedIds = expected.stream().map(Label::id).toList();

        List<Long> missing = expectedIds.stream()
                .filter(id -> !actualIds.contains(id))
                .distinct()
                .toList();

        List<Long> extra = actualIds.stream()
                .filter(id -> !expectedIds.contains(id))
                .distinct()
                .toList();

        List<Long> duplicates = actualIds.stream()
                .filter(id -> Collections.frequency(actualIds, id) > 1)
                .distinct()
                .toList();

        return new LabelMindMapFeedback(missing, duplicates, extra);
    }

    private List<Long> collectLabelIds(LabelMindMap labelMindMap) {
        List<Long> ids = new ArrayList<>();
        collectLabelIdsRecursive(labelMindMap.getRoot(), ids);
        return ids;
    }

    private void collectLabelIdsRecursive(LabelMindMapNode node, List<Long> ids) {
        if (node == null) return;
        ids.add(node.getLabelId());
        if (node.getChildren() != null) {
            for (LabelMindMapNode child : node.getChildren()) {
                collectLabelIdsRecursive(child, ids);
            }
        }
    }
}
