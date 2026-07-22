package ch.supsi.documentapplication.service.mindmap;

import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.MindMapNode;
import ch.supsi.documentapplication.model.Page;
import ch.supsi.documentapplication.model.PageMindMap;
import ch.supsi.documentapplication.model.PageMindMapNode;
import ch.supsi.documentapplication.model.PageTopicGroup;
import ch.supsi.documentapplication.model.learn.Topic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MindMapAssembler {

    public MindMap assemble(PageMindMap pageMindMap, List<Page> pages, List<Topic> topics) {
        if (pageMindMap == null || pageMindMap.getRoot() == null) {
            throw new IllegalStateException("Page mind map has no root");
        }

        Map<Long, List<Topic>> topicsByPageId = topics.stream().collect(Collectors.groupingBy(Topic::getPage));

        MindMap mindMap = new MindMap();
        mindMap.setRoot(buildMixedTree(pageMindMap.getRoot(), topicsByPageId));
        return mindMap;
    }

    private MindMapNode buildMixedTree(PageMindMapNode pageNode, Map<Long, List<Topic>> topicsByPageId) {
        MindMapNode node = MindMapNode.page(pageNode.getPageId());
        List<MindMapNode> children = new ArrayList<>();

        for (Topic topic : topicsByPageId.getOrDefault(pageNode.getPageId(), List.of())) {
            children.add(MindMapNode.topic(topic.getId()));
        }

        if (pageNode.getChildren() != null) {
            for (PageMindMapNode childPage : pageNode.getChildren()) {
                children.add(buildMixedTree(childPage, topicsByPageId));
            }
        }

        node.setChildren(children);
        return node;
    }

    public static List<PageTopicGroup> buildPageTopicGroups(List<Page> pages, List<Topic> topics) {
        Map<Long, List<Integer>> groups = new HashMap<>();
        pages.forEach(page -> groups.put(page.getId(), new ArrayList<>()));

        Map<Long, List<Topic>> topicsByPageId = topics.stream().collect(Collectors.groupingBy(Topic::getPage));

        for (Page page : pages) {
            List<Topic> pageTopics = topicsByPageId.getOrDefault(page.getId(), List.of());
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < pageTopics.size(); i++) {
                indices.add(i);
            }
            groups.put(page.getId(), indices);
        }

        return pages.stream()
                .map(page -> new PageTopicGroup(page.getId(), groups.get(page.getId())))
                .toList();
    }
}
