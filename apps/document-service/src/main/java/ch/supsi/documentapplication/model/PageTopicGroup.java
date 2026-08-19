package ch.supsi.documentapplication.model;

import java.util.List;

public record PageTopicGroup(Long pageId, List<Integer> topicIndices) {}
