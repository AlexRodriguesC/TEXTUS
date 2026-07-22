package ch.supsi.documentapplication.service.validator;

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException;
import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.model.PageMindMap;
import ch.supsi.documentapplication.model.PageMindMapNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PageMindMapValidator implements FeedbackAwareValidator<PageMindMap, List<Page>, PageMindMapFeedback> {

    @Override
    public void validate(PageMindMap actual, List<Page> expected) {
        if (actual == null || expected == null) {
            throw AIOutputMappingFailedException.create("page-mind-map", "page mind map or pages cannot be null");
        }

        PageMindMapFeedback feedback = extractFeedback(actual, expected);
        if (!feedback.isValid()) {
            throw AIOutputMappingFailedException.create("page-mind-map", "page mind map does not match expected pages");
        }
    }

    @Override
    public PageMindMapFeedback extractFeedback(PageMindMap actual, List<Page> expected) {
        if (actual == null || expected == null) {
            throw AIOutputMappingFailedException.create("page-mind-map", "page mind map or pages cannot be null");
        }

        List<Long> actualIds = collectPageIds(actual);
        List<Long> expectedIds = expected.stream().map(Page::getId).toList();

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

        return new PageMindMapFeedback(missing, duplicates, extra);
    }

    private List<Long> collectPageIds(PageMindMap pageMindMap) {
        List<Long> ids = new ArrayList<>();
        collectPageIdsRecursive(pageMindMap.getRoot(), ids);
        return ids;
    }

    private void collectPageIdsRecursive(PageMindMapNode node, List<Long> ids) {
        if (node == null) return;
        ids.add(node.getPageId());
        if (node.getChildren() != null) {
            for (PageMindMapNode child : node.getChildren()) {
                collectPageIdsRecursive(child, ids);
            }
        }
    }
}
