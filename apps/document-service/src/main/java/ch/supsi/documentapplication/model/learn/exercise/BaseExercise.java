package ch.supsi.documentapplication.model.learn.exercise;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
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
    @JsonSubTypes.Type(value = PairExercise.class, name = "PAIR")
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

    @JsonIgnore
    @Setter
    protected boolean correct = false;

    @Setter
    @JsonPropertyDescription("The question or task text shown to the user. Always use this exact field name.")
    protected String request;

    @Setter
    @JsonPropertyDescription(
            "The single correct answer. Must always be a flat value matching the declared type, never a nested object.")
    protected T answer;

    @Setter
    @JsonPropertyDescription(
            "A concise explanation of why the answer is correct, grounded only in the provided source content. This field never appears anywhere except at this top level.")
    protected String explanation;

    @Override
    public String getExplanation() {
        return explanation;
    }

    @Override
    public String getRequest() {
        return request;
    }

    @Override
    public T getAnswer() {
        return answer;
    }
}
