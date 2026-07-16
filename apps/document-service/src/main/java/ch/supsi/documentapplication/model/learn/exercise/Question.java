package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;

import java.util.List;

public class Question extends BaseExercise<String> {

    private List<String> distractor;

    @Override
    public ExerciseType getType() {
        return ExerciseType.QUESTION;
    }

    @Override
    public double getGuessProbability() {
        return 1.0 / (1 + distractor.size());
    }
}
