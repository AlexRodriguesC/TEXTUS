package ch.supsi.documentapplication.service.validator;

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException;
import ch.supsi.documentapplication.model.learn.Chapter;
import ch.supsi.documentapplication.service.async.learningpipeline.Label;
import java.util.List;

public class ChapterValidator implements FeedbackAwareValidator<List<Chapter>, List<Label>, ChapterFeedback> {

    @Override
    public void validate(List<Chapter> actual, List<Label> expected) {
        if (actual == null || expected == null) {
            throw AIOutputMappingFailedException.create("chapter", "chapters or labels cannot be null");
        }

        ChapterFeedback feedback = extractFeedback(actual, expected);
        if (!feedback.isValid()) {
            throw AIOutputMappingFailedException.create("chapter", "chapters and labels do not match");
        }
    }

    @Override
    public ChapterFeedback extractFeedback(List<Chapter> actual, List<Label> expected) {
        if (actual == null || expected == null) {
            throw AIOutputMappingFailedException.create("chapter", "chapters or labels cannot be null");
        }

        List<Long> actualIds =
                actual.stream().flatMap(ch -> ch.getLabels().stream()).toList();
        List<Long> expectedIds = expected.stream().map(Label::id).toList();

        List<Long> missing = expectedIds.stream()
                .filter(id -> !actualIds.contains(id))
                .distinct()
                .toList();

        List<Long> extra = actualIds.stream()
                .filter(id -> !expectedIds.contains(id))
                .distinct()
                .toList();

        List<Long> duplicates = actualIds.stream()
                .filter(id -> java.util.Collections.frequency(actualIds, id) > 1)
                .distinct()
                .toList();

        return new ChapterFeedback(missing, duplicates, extra);
    }
}
