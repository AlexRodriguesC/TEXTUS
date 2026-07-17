package ch.supsi.documentapplication.controller.DTO.response.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PairResponseDTO(
        @NotNull Long id, 
        @NotNull Long topicId,
        @NotNull @NotEmpty String request, 
        boolean completed, 
        @NotNull ExerciseType type, 
        @NotNull @NotEmpty List<@NotNull @NotEmpty String> left, 
        @NotNull @NotEmpty List<@NotNull @NotEmpty String> right
) implements ExerciseDTO {
}
