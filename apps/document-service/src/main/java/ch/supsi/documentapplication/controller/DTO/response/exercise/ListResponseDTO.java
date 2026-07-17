package ch.supsi.documentapplication.controller.DTO.response.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import ch.supsi.documentapplication.model.learn.LessonType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ListResponseDTO(Long id,
                              String request, boolean completed, ExerciseType type, List<String> answer)
        implements ExerciseDTO {
}
