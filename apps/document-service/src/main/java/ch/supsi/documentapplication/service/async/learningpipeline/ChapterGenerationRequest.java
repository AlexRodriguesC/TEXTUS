package ch.supsi.documentapplication.service.async.learningpipeline;

import java.util.List;

public record ChapterGenerationRequest(List<Label> labels, int maxChapters) {}
