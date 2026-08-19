package ch.supsi.documentapplication.exception;

import org.springframework.http.HttpStatus;

public class LessonAttemptsExhaustedException extends ApplicationException {
    public LessonAttemptsExhaustedException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public static LessonAttemptsExhaustedException lessonAttemptsExhausted() {
        return new LessonAttemptsExhaustedException("Exhausted attempts for this lesson. Generate a new one.");
    }
}
