package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.controller.DTO.response.AttemptRequestDTO;
import ch.supsi.documentapplication.controller.DTO.response.AttemptResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.ExerciseResponseDTO;
import ch.supsi.documentapplication.model.learn.Lesson;

import java.util.List;

public interface ILearningService {
    Lesson getLesson(String owner, Long spaceId, Long nodeId);

    ExerciseResponseDTO getNextExercise(String owner, Long spaceId, Long nodeId);

    List<ExerciseResponseDTO> getExercises(String owner, Long spaceId, Long nodeId);

    AttemptResponseDTO submitAttempt(String owner, Long spaceId, Long nodeId, AttemptRequestDTO request);
}
