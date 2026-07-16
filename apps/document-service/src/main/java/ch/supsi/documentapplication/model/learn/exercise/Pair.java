package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;

public class Pair extends BaseExercise<org.apache.commons.lang3.tuple.Pair<String, String>> {

    @Override
    public ExerciseType getType() {
        return ExerciseType.PAIR;
    }

    @Override
    public double getGuessProbability() {
        return 0.05;
    }
}
