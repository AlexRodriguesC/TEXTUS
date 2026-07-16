package ch.supsi.documentapplication.model.learn.exercise;

public interface IExercise<T> {

    String getRequest();

    T getAnswer();
}
