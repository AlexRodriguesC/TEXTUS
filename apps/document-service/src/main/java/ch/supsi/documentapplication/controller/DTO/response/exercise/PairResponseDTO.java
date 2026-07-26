package ch.supsi.documentapplication.controller.DTO.response.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import ch.supsi.documentapplication.model.learn.exercise.LeftRight;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.util.List;

@Valid
public record PairResponseDTO(
        @NotNull Long id, 
        @NotNull Long topicId,
        @NotNull @NotEmpty String request, 
        boolean completed, 
        @NotNull ExerciseType type,
        @NotNull @NotEmpty List<@NotNull LeftRight> pairs
) implements ExerciseDTO {
}
