package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.PathSettings;
import ch.supsi.documentapplication.model.learn.Chapter;
import ch.supsi.documentapplication.model.learn.LearningPath;

public interface IDatabasePathStorage {

    void create(Long pathId, Chapter chapter, Long spaceId);

    Long createLearningPath(Long spaceId, Long executionId);

    LearningPath read(Long spaceId);

    void delete(Long spaceId);

    void updateSettings(Long id, PathSettings settings);
}
