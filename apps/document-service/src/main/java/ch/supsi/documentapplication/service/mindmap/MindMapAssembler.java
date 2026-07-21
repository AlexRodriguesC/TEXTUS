package ch.supsi.documentapplication.service.mindmap;

import ch.supsi.documentapplication.model.LabelMindMap;
import ch.supsi.documentapplication.model.LabelMindMapNode;
import ch.supsi.documentapplication.model.MindMap;
import ch.supsi.documentapplication.model.MindMapNode;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.service.async.learningpipeline.Label;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MindMapAssembler {

    public MindMap assemble(LabelMindMap labelMindMap, List<Topic> topics, List<Label> labels) {
        if (labelMindMap == null || labelMindMap.getRoot() == null) {
            throw new IllegalStateException("Label mind map has no root");
        }

        Map<Long, String> labelNameById = labels.stream().collect(Collectors.toMap(Label::id, Label::name));
        Map<Long, Integer> labelDepth = computeLabelDepths(labelMindMap.getRoot());
        Map<Long, List<Topic>> topicsByLabelId = assignTopicsToLabels(labelMindMap, topics, labelNameById, labelDepth);
        Map<Long, Long> headTopicByLabelId = electHeadTopics(topicsByLabelId, labelNameById);

        MindMap mindMap = new MindMap();
        mindMap.setRoot(buildTopicTree(labelMindMap.getRoot(), topicsByLabelId, headTopicByLabelId));
        return mindMap;
    }

    private Map<Long, Integer> computeLabelDepths(LabelMindMapNode root) {
        Map<Long, Integer> depths = new HashMap<>();
        computeDepth(root, 0, depths);
        return depths;
    }

    private void computeDepth(LabelMindMapNode node, int depth, Map<Long, Integer> depths) {
        if (node == null) return;
        depths.put(node.getLabelId(), depth);
        if (node.getChildren() != null) {
            for (LabelMindMapNode child : node.getChildren()) {
                computeDepth(child, depth + 1, depths);
            }
        }
    }

    private Map<Long, List<Topic>> assignTopicsToLabels(
            LabelMindMap labelMindMap,
            List<Topic> topics,
            Map<Long, String> labelNameById,
            Map<Long, Integer> labelDepth) {
        Set<Long> labelIds = labelMindMap.getAllLabelIds();
        Map<Long, List<Topic>> assignment = new HashMap<>();
        labelIds.forEach(id -> assignment.put(id, new ArrayList<>()));

        for (Topic topic : topics) {
            if (topic.getLabels() == null || topic.getLabels().isEmpty()) {
                throw new IllegalStateException("Topic with id " + topic.getId() + " has no labels");
            }

            Long bestLabelId = null;
            int bestScore = -1;
            int bestDepth = -1;

            for (Long labelId : labelIds) {
                String labelName = labelNameById.get(labelId);
                if (labelName == null) continue;

                int score = (int)
                        topic.getLabels().stream().filter(labelName::equals).count();
                if (score > bestScore || (score == bestScore && labelDepth.getOrDefault(labelId, 0) > bestDepth)) {
                    bestScore = score;
                    bestDepth = labelDepth.getOrDefault(labelId, 0);
                    bestLabelId = labelId;
                }
            }

            if (bestLabelId == null || bestScore == 0) {
                throw new IllegalStateException("Topic with id " + topic.getId() + " matches no label in the tree");
            }

            assignment.get(bestLabelId).add(topic);
        }

        return assignment;
    }

    private Map<Long, Long> electHeadTopics(Map<Long, List<Topic>> topicsByLabelId, Map<Long, String> labelNameById) {
        Map<Long, Long> headTopicByLabelId = new HashMap<>();

        for (Map.Entry<Long, List<Topic>> entry : topicsByLabelId.entrySet()) {
            List<Topic> assigned = entry.getValue();
            if (assigned.isEmpty()) {
                continue;
            }

            String labelName = labelNameById.get(entry.getKey());
            Topic head = assigned.stream()
                    .max(Comparator.comparingInt(topic -> (int)
                            topic.getLabels().stream().filter(labelName::equals).count()))
                    .orElseThrow();
            headTopicByLabelId.put(entry.getKey(), head.getId());
        }

        return headTopicByLabelId;
    }

    private MindMapNode buildTopicTree(
            LabelMindMapNode labelNode, Map<Long, List<Topic>> topicsByLabelId, Map<Long, Long> headTopicByLabelId) {
        Long headTopicId = headTopicByLabelId.get(labelNode.getLabelId());
        List<Topic> assignedTopics = topicsByLabelId.getOrDefault(labelNode.getLabelId(), List.of());

        List<MindMapNode> childLabelTopicNodes = new ArrayList<>();
        if (labelNode.getChildren() != null) {
            for (LabelMindMapNode child : labelNode.getChildren()) {
                MindMapNode childTopicNode = buildTopicTree(child, topicsByLabelId, headTopicByLabelId);
                if (childTopicNode != null) {
                    childLabelTopicNodes.add(childTopicNode);
                }
            }
        }

        if (headTopicId == null) {
            if (childLabelTopicNodes.isEmpty()) {
                return null;
            }
            if (childLabelTopicNodes.size() == 1) {
                return childLabelTopicNodes.getFirst();
            }
            MindMapNode virtualRoot =
                    new MindMapNode(childLabelTopicNodes.getFirst().getTopicId());
            virtualRoot.setChildren(new ArrayList<>(childLabelTopicNodes.subList(1, childLabelTopicNodes.size())));
            return virtualRoot;
        }

        MindMapNode node = new MindMapNode(headTopicId);
        List<MindMapNode> children = new ArrayList<>();

        for (Topic topic : assignedTopics) {
            if (!topic.getId().equals(headTopicId)) {
                children.add(new MindMapNode(topic.getId()));
            }
        }
        children.addAll(childLabelTopicNodes);
        node.setChildren(children);
        return node;
    }

    public static List<Label> buildLabels(List<Topic> topics) {
        List<String> distinctLabels = topics.stream()
                .flatMap(topic -> topic.getLabels().stream())
                .distinct()
                .toList();

        return IntStream.range(0, distinctLabels.size())
                .mapToObj(i -> new Label((long) i, distinctLabels.get(i)))
                .toList();
    }

    public static List<ch.supsi.documentapplication.model.LabelTopicGroup> buildLabelTopicGroups(
            List<Topic> topics, List<Label> labels) {
        Map<String, Long> labelIdByName = labels.stream().collect(Collectors.toMap(Label::name, Label::id));

        Map<Long, List<Integer>> groups = new HashMap<>();
        labels.forEach(label -> groups.put(label.id(), new ArrayList<>()));

        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            Set<Long> addedForTopic = new HashSet<>();
            for (String labelName : topic.getLabels()) {
                Long labelId = labelIdByName.get(labelName);
                if (labelId != null && addedForTopic.add(labelId)) {
                    groups.get(labelId).add(i);
                }
            }
        }

        return labels.stream()
                .map(label ->
                        new ch.supsi.documentapplication.model.LabelTopicGroup(label.id(), groups.get(label.id())))
                .toList();
    }
}
