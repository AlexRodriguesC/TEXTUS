package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.controller.LearningController;
import ch.supsi.documentapplication.model.LessonStatus;
import ch.supsi.documentapplication.model.learn.Lesson;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents the state of a node's lesson, including BKT mastery and completion status")
public record LessonResponseDTO(
        @Schema(description = "Identifier of the node this lesson belongs to") Long nodeId,
        @Schema(description = "Current lesson status", example = "IN_PROGRESS") LessonStatus status,
        @Schema(description = "Current BKT mastery probability, between 0 and 1", example = "0.42") double mastery,
        @Schema(description = "Number of attempts used so far in this lesson", example = "3") int attemptsUsed,
        @Schema(description = "Maximum number of attempts allowed before the lesson is exhausted", example = "10") int maxAttempts
) {

    public static LessonResponseDTO from(Lesson lesson) {
        return new LessonResponseDTO(lesson.getNodeId(), lesson.getStatus(), lesson.getMastery(), lesson.getAttemptsUsed(), lesson.getMaxAttempts());
    }
}
