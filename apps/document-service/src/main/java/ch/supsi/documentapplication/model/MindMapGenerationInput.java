package ch.supsi.documentapplication.model;

import ch.supsi.documentapplication.model.learn.Topic;
import java.util.List;

public record MindMapGenerationInput(List<Page> pages, List<Topic> topics) {}
