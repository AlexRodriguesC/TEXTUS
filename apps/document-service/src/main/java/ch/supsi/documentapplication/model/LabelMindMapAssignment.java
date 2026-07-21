package ch.supsi.documentapplication.model;

import java.util.List;

public record LabelMindMapAssignment(int parentNodeIndex, List<Long> labelIds) {}
