package ch.supsi.documentapplication.model.learn;

import ch.supsi.documentapplication.model.LessonStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Lesson {
    private Long nodeId;
    private LessonStatus status;
    private double mastery;
    private int attemptsUsed;
    private int maxAttempts;
}
