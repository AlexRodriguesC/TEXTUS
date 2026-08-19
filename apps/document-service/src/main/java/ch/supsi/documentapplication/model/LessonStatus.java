package ch.supsi.documentapplication.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status of a lesson")
public enum LessonStatus {
    IN_PROGRESS,
    MASTERED,
    EXHAUSTED
}
