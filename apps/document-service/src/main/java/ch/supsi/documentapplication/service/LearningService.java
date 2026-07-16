package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.controller.DTO.response.AttemptRequestDTO;
import ch.supsi.documentapplication.controller.DTO.response.AttemptResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.ExerciseResponseDTO;
import ch.supsi.documentapplication.dataaccess.DatabasePathStorage;
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage;
import ch.supsi.documentapplication.model.PathSettings;
import ch.supsi.documentapplication.model.learn.Lesson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LearningService extends Service implements ILearningService {

    private final DatabasePathStorage databasePathStorage;

    @Autowired
    public LearningService(IDatabaseSpaceStorage databaseSpaceStorage, DatabasePathStorage databasePathStorage) {
        super(databaseSpaceStorage);
        this.databasePathStorage = databasePathStorage;
    }

    @Override
    public Lesson getLesson(String owner, Long spaceId, Long nodeId) {
        var space = getSpaceOrThrow(owner, spaceId);
        var path = databasePathStorage.read(space.getId());



        return null;
    }

    @Override
    public ExerciseResponseDTO getNextExercise(String owner, Long spaceId, Long nodeId) {
        var space = getSpaceOrThrow(owner, spaceId);


        return null;
    }

    @Override
    public List<ExerciseResponseDTO> getExercises(String owner, Long spaceId, Long nodeId) {
        var space = getSpaceOrThrow(owner, spaceId);
        return List.of();
    }

    @Override
    public AttemptResponseDTO submitAttempt(String owner, Long spaceId, Long nodeId, AttemptRequestDTO request) {
        var space = getSpaceOrThrow(owner, spaceId);
        return null;
    }
}
