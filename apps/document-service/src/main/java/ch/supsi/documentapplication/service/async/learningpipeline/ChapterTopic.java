package ch.supsi.documentapplication.service.async.learningpipeline;

/**
 * One topic entry inside a chapter-boundary detection window.
 *
 * <p>{@code position} is 1-based within the current window. {@code labels} is the
 * space-separated concatenation of the topic's short labels.
 */
public record ChapterTopic(int position, String labels) {}
