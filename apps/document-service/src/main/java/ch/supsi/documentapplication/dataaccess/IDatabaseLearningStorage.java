package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Chapter;
import ch.supsi.documentapplication.model.LearningPath;

public interface IDatabaseLearningStorage {

    void create(Long pathId, Chapter chapter, Long spaceId);

    Long createLearningPath(Long spaceId, Long executionId);

    LearningPath read(Long spaceId);

    void delete(Long spaceId);
}
