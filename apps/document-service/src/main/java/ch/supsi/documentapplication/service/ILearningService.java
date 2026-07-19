package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.controller.DTO.response.AttemptRequestDTO;
import ch.supsi.documentapplication.controller.DTO.response.AttemptResponseDTO;
import ch.supsi.documentapplication.exception.ApplicationException;
import ch.supsi.documentapplication.model.learn.Lesson;
import ch.supsi.documentapplication.model.learn.exercise.BaseExercise;
import java.util.List;

public interface ILearningService {

    Lesson getLesson(String owner, Long spaceId, Long nodeId) throws ApplicationException;

    BaseExercise<?> getNextExercise(String owner, Long spaceId, Long nodeId) throws ApplicationException;

    List<BaseExercise<?>> getExercises(String owner, Long spaceId, Long nodeId) throws ApplicationException;

    AttemptResponseDTO submitAttempt(String owner, Long spaceId, Long nodeId, AttemptRequestDTO request)
            throws ApplicationException;

    void deleteExercises(String owner, Long spaceId, Long lessonId);
}
