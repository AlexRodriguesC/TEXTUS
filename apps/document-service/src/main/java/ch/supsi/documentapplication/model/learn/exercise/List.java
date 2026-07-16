package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;

public class List extends BaseExercise<java.util.List<String>> {
    @Override
    public ExerciseType getType() {
        return ExerciseType.LIST;
    }

    @Override
    public double getGuessProbability() {
        return 0.05;
    }
}
