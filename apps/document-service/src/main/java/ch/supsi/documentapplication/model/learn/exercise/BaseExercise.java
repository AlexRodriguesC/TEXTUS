package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "exerciseType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Question.class, name = "QUESTION"),
    @JsonSubTypes.Type(value = List.class, name = "LIST"),
    @JsonSubTypes.Type(value = Group.class, name = "GROUP"),
    @JsonSubTypes.Type(value = Pair.class, name = "PAIR")
})
public abstract class BaseExercise<T> implements IExercise<T> {

    @JsonIgnore
    @Setter
    protected Long id;

    @JsonIgnore
    @Setter
    protected Integer index;

    @JsonIgnore
    @Setter
    protected Long topicId;

    @JsonIgnore
    @Setter
    protected boolean completed = false;

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
