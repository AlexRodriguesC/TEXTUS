package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Attempt;
import ch.supsi.documentapplication.model.learn.Lesson2;
import ch.supsi.documentapplication.model.learn.exercise.BaseExercise;
import java.util.List;

public interface IDatabaseLearningStorage {
    List<BaseExercise<?>> getExercises(Long spaceId, Long nodeId);

    BaseExercise<?> createExercise(BaseExercise<?> exercise, Long lessonId);

    Lesson2 readLesson(Long lessonId);

    void update(Lesson2 lesson);

    void create(Attempt<?> attempt, String userId, Long lessonId, Long exerciseId);

    void updateExercise(BaseExercise<?> exercise);
}
