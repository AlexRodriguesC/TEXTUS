package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.model.PathSettings;
import ch.supsi.documentapplication.model.learn.LearningPath;
import ch.supsi.documentapplication.model.learn.LearningPathStatus;
import jakarta.validation.constraints.NotNull;

public interface IPathService {

    void createLearningPath(String owner, Long spaceId);

    LearningPath getLearningPath(String owner, Long spaceId);

    LearningPathStatus getLearningPathStatus(String owner, @NotNull Long spaceId);

    void deleteLearningPath(String owner, @NotNull Long spaceId);

    void updateSettings(String owner, @NotNull Long spaceId, PathSettings settings);
}
