package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Attempt;
import ch.supsi.documentapplication.model.learn.Lesson2;
import ch.supsi.documentapplication.model.learn.exercise.Exercise;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DatabaseLearningStorage implements IDatabaseLearningStorage {

    @Override
    public List<Exercise> getExercises(Long spaceId, Long nodeId) {
        return List.of();
    }

    @Override
    public Exercise createExercise(Exercise exercise) {
        return null;
    }

    @Override
    public Lesson2 readLesson(Long lessonId) {
        return null;
    }

    @Override
    public void update(Lesson2 lesson) {

    }

    @Override
    public void create(Attempt<?> attempt) {

    }
}
