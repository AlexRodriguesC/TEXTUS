package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Chapter;
import ch.supsi.documentapplication.model.LearningPath;

public interface IDatabaseLearningStorage {

    void create(Chapter chapter, Long spaceId);

    LearningPath read(Long spaceId);

    void delete(Long spaceId);
}
