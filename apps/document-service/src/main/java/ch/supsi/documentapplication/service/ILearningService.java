package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.controller.DTO.response.AttemptRequestDTO;
import ch.supsi.documentapplication.controller.DTO.response.AttemptResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.ExerciseResponseDTO;
import ch.supsi.documentapplication.exception.ApplicationException;
import ch.supsi.documentapplication.model.learn.Lesson2;
import ch.supsi.documentapplication.model.learn.exercise.Exercise;

import java.util.List;

public interface ILearningService {

    Lesson2 getLesson2(String owner, Long spaceId, Long nodeId) throws ApplicationException;

    Exercise getNextExercise(String owner, Long spaceId, Long nodeId) throws ApplicationException;

    List<Exercise> getExercises(String owner, Long spaceId, Long nodeId) throws ApplicationException;

    AttemptResponseDTO submitAttempt(String owner, Long spaceId, Long nodeId, AttemptRequestDTO request) throws ApplicationException;
}
