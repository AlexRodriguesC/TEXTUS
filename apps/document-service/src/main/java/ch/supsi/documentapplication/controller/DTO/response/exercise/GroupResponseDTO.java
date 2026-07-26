package ch.supsi.documentapplication.controller.DTO.response.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@Valid
public record GroupResponseDTO(
        @NotNull Long id, 
        @NotNull Long topicId,
        @NotNull @NotEmpty String request, 
        boolean completed, 
        @NotNull ExerciseType type,
        @NotNull @NotEmpty Map<@NotNull @NotEmpty String, @NotNull @NotEmpty List<String>> groups
) implements ExerciseDTO {
}
