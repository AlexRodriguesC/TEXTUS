package ch.supsi.documentapplication.service.async.learningpipeline;

import java.util.List;

public record ChapterGenerationRequest(List<ChapterTopic> topics) {}
