package ch.supsi.documentapplication.controller.DTO.response.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;

public interface ExerciseDTO {
    Long id();
    ExerciseType type();
    boolean completed();
}
