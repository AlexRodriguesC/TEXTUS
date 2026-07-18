package ch.supsi.documentapplication.exception;

import org.springframework.http.HttpStatus;

public class LessonPrerequisiteNotSatisfiedException extends ApplicationException {

    public LessonPrerequisiteNotSatisfiedException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public static LessonPrerequisiteNotSatisfiedException lessonPrerequisiteNotSatisfied(
            Long requestedLessonId, Long prerequisiteLessonId) {
        return new LessonPrerequisiteNotSatisfiedException(format(
                "Lesson with id %d cannot be started because prerequisite lesson with id %d is not completed",
                requestedLessonId, prerequisiteLessonId));
    }
}
