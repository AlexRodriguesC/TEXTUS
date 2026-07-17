package ch.supsi.documentapplication.controller.DTO.response.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;

import java.util.List;

public record QuestionResponseDTO(Long id,
                                  String request, boolean completed, ExerciseType type, List<String> answers) implements ExerciseDTO {
}
