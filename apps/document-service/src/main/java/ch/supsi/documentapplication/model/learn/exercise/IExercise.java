package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;

public interface IExercise<T> {

    String getRequest();

    T getAnswer();

    String getExplanation();

    boolean isAnswerCorrect(T userResponse);

    ExerciseType getType();

    double getGuessProbability();
}
