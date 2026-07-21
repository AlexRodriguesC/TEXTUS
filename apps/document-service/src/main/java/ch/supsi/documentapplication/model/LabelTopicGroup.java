package ch.supsi.documentapplication.model;

import java.util.List;

public record LabelTopicGroup(Long labelId, List<Integer> topicIndices) {}
