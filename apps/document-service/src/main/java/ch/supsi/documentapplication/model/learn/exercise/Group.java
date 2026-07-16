package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;

import java.util.List;
import java.util.Map;

public class Group extends BaseExercise<Map<String, List<String>>>  {

    @Override
    public ExerciseType getType() {
        return ExerciseType.GROUP;
    }

    @Override
    public double getGuessProbability() {
        return 0.05;
    }
}
