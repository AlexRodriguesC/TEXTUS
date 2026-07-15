package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.model.LearningPath;
import ch.supsi.documentapplication.model.LearningPathStatus;
import ch.supsi.documentapplication.model.PathSettings;
import jakarta.validation.constraints.NotNull;

public interface ILearningService {

    void createLearningPath(String owner, Long spaceId);

    LearningPath getLearningPath(String owner, Long spaceId);

    LearningPathStatus getLearningPathStatus(String owner, @NotNull Long spaceId);

    void deleteLearningPath(String owner, @NotNull Long spaceId);

    void updateSettings(String owner, @NotNull Long spaceId, PathSettings settings);
}
