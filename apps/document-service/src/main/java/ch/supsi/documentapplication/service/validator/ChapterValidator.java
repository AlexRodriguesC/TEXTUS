package ch.supsi.documentapplication.service.validator;

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException;
import ch.supsi.documentapplication.model.Chapter;
import ch.supsi.documentapplication.service.async.learningpipeline.Label;
import java.util.List;

public class ChapterValidator implements Validator<List<Chapter>, List<Label>> {

    public void validate(List<Chapter> actual, List<Label> expected) {
        if (actual == null || expected == null) {
            throw AIOutputMappingFailedException.create("chapter", "chapters or labels cannot be null");
        }

        List<Long> actualIds =
                actual.stream().flatMap(ch -> ch.getLabels().stream()).toList();
        List<Long> expectedIds = expected.stream().map(Label::id).toList();

        if (actualIds.size() != expectedIds.size()) {
            throw AIOutputMappingFailedException.create("chapter", "chapters and labels do not match");
        }

        for (Long actualId : actualIds) {
            if (!expectedIds.contains(actualId)) {
                throw AIOutputMappingFailedException.create("chapter", "chapters do not match");
            }
        }
    }
}
