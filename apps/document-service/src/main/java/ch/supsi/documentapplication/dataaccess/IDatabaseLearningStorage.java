package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Attempt;
import ch.supsi.documentapplication.model.learn.Lesson;
import ch.supsi.documentapplication.model.learn.exercise.BaseExercise;
import java.util.List;

public interface IDatabaseLearningStorage {
    List<BaseExercise<?>> getExercises(Long lessonId);

    BaseExercise<?> createExercise(BaseExercise<?> exercise, Long lessonId);

    Lesson readLesson(Long lessonId);

    void update(Lesson lesson);

    void create(Attempt<?> attempt, String userId, Long lessonId, Long exerciseId);

    void updateExercise(BaseExercise<?> exercise);

    void deleteExercises(Long lessonId);
}
