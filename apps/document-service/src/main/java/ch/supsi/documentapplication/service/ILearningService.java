package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.model.LearningPath;
import ch.supsi.documentapplication.model.LearningPathStatus;
import jakarta.validation.constraints.NotNull;

public interface ILearningService {

    void createLearningPath(String username, Long spaceId);

    LearningPath getLearningPath(String username, Long spaceId);

    LearningPathStatus getLearningPathStatus(String username, @NotNull Long spaceId);

    void deleteLearningPath(String username, @NotNull Long spaceId);
}
