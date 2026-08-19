package ch.supsi.documentapplication.model;

import java.util.List;

public record PageMindMapAssignment(int parentNodeIndex, List<Long> pageIds) {}
