package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;

public abstract class BaseExercise<T> implements IExercise<T> {

    protected String request;
    protected Answer<T> answer;

    @Override
    public String getRequest() {
        return request;
    }

    @Override
    public T getAnswer() {
        return answer.getAnswer();
    }

    public abstract ExerciseType getType();
    public abstract double getGuessProbability();
}
