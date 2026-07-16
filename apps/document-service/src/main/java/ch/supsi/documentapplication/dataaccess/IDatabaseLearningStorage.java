package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Attempt;
import ch.supsi.documentapplication.model.learn.Lesson2;
import ch.supsi.documentapplication.model.learn.exercise.Exercise;

import java.util.List;

public interface IDatabaseLearningStorage {
    List<Exercise> getExercises(Long spaceId, Long nodeId);

    Exercise createExercise(Exercise exercise);

    Lesson2 readLesson(Long lessonId);
    void update(Lesson2 lesson);

    void create(Attempt<?> attempt);

    void updateExercise(Exercise exercise);
}
