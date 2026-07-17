package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.learn.Lesson;
import ch.supsi.documentapplication.model.learn.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Represents the current state of a lesson (node) within a learning path")
public record LessonResponseDTO(
        @Schema(description = "Identifier of the lesson (node)") Long id,
        @Schema(description = "Type of the lesson", example = "LESSON") LessonType type,
        boolean completed,
        @Schema(description = "ID of the current topic being worked on") Long currentTopic,
        @Schema(description = "Current BKT mastery probability, between 0 and 1", example = "0.42") double mastery,
        @Schema(description = "Number of attempts remaining before the lesson is exhausted", example = "7") int attemptsLeft
) {

    public static LessonResponseDTO from(Lesson lesson) {
        return new LessonResponseDTO(
                lesson.getId(),
                lesson.getType(),
                lesson.isMastered(),
                lesson.getCurrentTopic(),
                lesson.getMastery(),
                lesson.getAttemptsLeft()
        );
    }
}