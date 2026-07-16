package ch.supsi.documentapplication.model.learn;

import ch.supsi.documentapplication.model.LessonStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Lesson {
    private Long nodeId;
    private Topic currentTopic;
    private LessonStatus status;
    private double mastery;
    private int attemptsUsed;
    private int maxAttempts;
}
