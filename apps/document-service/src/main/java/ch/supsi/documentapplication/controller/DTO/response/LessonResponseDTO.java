package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.learn.Lesson;
import ch.supsi.documentapplication.model.learn.LessonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.util.List;

@Valid
@Schema(description = "Represents the current state of a lesson (node) within a learning path")
public record LessonResponseDTO(
        @Schema(description = "Identifier of the lesson (node)") @NotNull Long id,
        @Schema(description = "Unique identifier of the chapter containing this lesson") @NotNull Long chapterId,
        @Schema(description = "Index (position) of the lesson inside its chapter") @NotNull Integer index,
        @Schema(description = "Type of the lesson", example = "LESSON") @NotNull LessonType type,
        @NotNull boolean completed,
        @Schema(description = "ID of the current topic being worked on") @NotNull Long currentTopic,
        @Schema(description = "List of topics for this lesson") List<@NotNull Long> topics,
        @Schema(description = "Current BKT mastery probability, between 0 and 1", example = "0.42") @NotNull double mastery,
        @Schema(description = "Total number of attempts allowed for this lesson", example = "10") @NotNull int maxAttempts,
        @Schema(description = "Number of attempts remaining before the lesson is exhausted", example = "7") @NotNull int attemptsLeft
) {

    public static LessonResponseDTO from(Lesson lesson) {
        return new LessonResponseDTO(
                lesson.getId(),
                lesson.getChapterId(),
                lesson.getIndex(),
                lesson.getType(),
                lesson.isMastered(),
                lesson.getCurrentTopic(),
                lesson.getTopics(),
                lesson.getMastery(),
                lesson.getMaxAttempts(),
                lesson.getAttemptsLeft()
        );
    }
}