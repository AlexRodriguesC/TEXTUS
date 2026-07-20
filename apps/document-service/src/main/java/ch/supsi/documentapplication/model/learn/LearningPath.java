package ch.supsi.documentapplication.model.learn;

import ch.supsi.documentapplication.model.PathSettings;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LearningPath {

    private Long jobId;
    private PathSettings settings;
    private List<Chapter> chapters;

    public Lesson getLesson(Long lessonId) {
        return chapters.stream()
                .flatMap(chapter -> chapter.getLessons().stream())
                .filter(lesson -> lesson.getId().equals(lessonId))
                .findFirst()
                .orElse(null);
    }

    public Lesson getPrerequisiteLesson(Long lessonId) {
        Lesson lesson = getLesson(lessonId);
        if (lesson == null) {
            return null;
        }

        Chapter chapter = chapters.stream()
                .filter(c -> c.getLessons().contains(lesson))
                .findFirst()
                .orElse(null);

        if (chapter == null) {
            return null;
        }

        return chapter.getLessons().stream()
                .filter(l -> l.getIndex() == lesson.getIndex() - 1)
                .findFirst()
                .orElse(null);
    }
}
