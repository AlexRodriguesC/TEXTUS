package ch.supsi.documentapplication.controller.DTO.response.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.util.List;

@Valid
public record QuestionResponseDTO(
        @NotNull Long id,
        @NotNull Long topicId,
        @NotNull @NotEmpty String request,
        boolean completed,
        @NotNull ExerciseType type,
        @NotNull @NotEmpty String answer,
        @NotNull @NotEmpty List<String> distractors
) implements ExerciseDTO {
}
