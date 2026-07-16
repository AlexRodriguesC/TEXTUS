package ch.supsi.documentapplication.model.learn.exercise;

import lombok.Getter;

@Getter
public abstract class Answer<T> {

    protected T answer;
    protected String explanation;

}
