package ch.supsi.documentapplication.controller.DTO.response.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;

import java.util.List;
import java.util.Map;

public record PairResponseDTO(Long id,
                              String request, boolean completed, ExerciseType type, List<String> left, List<String> right) implements ExerciseDTO {
}
