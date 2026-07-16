package ch.supsi.documentapplication.controller.DTO.response;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import ch.supsi.documentapplication.model.learn.exercise.BaseExercise;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents a single exercise served to the user for a lesson")
public record ExerciseResponseDTO(
        @Schema(description = "Unique identifier of the exercise") Long exerciseId,
        @Schema(description = "Exercise family type", example = "pair") ExerciseType family,
        @Schema(description = "Family-specific exercise content payload") BaseExercise<?> content,
        @Schema(description = "Whether this exercise has already been attempted", example = "false") boolean completed
) {

    public static ExerciseResponseDTO from(BaseExercise<?> exercise) {
        return new ExerciseResponseDTO(exercise.getId(), exercise.getType(), exercise , exercise.isCompleted());
    }
}
