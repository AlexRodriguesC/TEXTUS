package ch.supsi.documentapplication.model;

import ch.supsi.documentapplication.controller.DTO.response.AttemptRequestDTO;
import ch.supsi.documentapplication.model.learn.ExerciseType;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.model.learn.exercise.*;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Objects;

@Getter
public class Attempt<T> {

    private BaseExercise<T> exercise;
    private T userResponse;
    private Topic topic;

    @Setter
    private String feedback;

    private Attempt(BaseExercise<T> exercise,
                    T userResponse, Topic topic) {
        this.exercise = exercise;
        this.userResponse = userResponse;
        this.topic = topic;
    }

    @SuppressWarnings("unchecked")
    public static Attempt<?> from(AttemptRequestDTO request, BaseExercise<?> persistedExercise, Topic topic) {
        return switch (request.exerciseType()) {
            case QUESTION -> build(request, (BaseExercise<String>) persistedExercise, String.class, topic);
            case LIST -> build(request, (BaseExercise<java.util.List<String>>) persistedExercise, new TypeReference<>() {
            }, topic);
            case GROUP -> build(request, (BaseExercise<Map<String, java.util.List<String>>>) persistedExercise, new TypeReference<>() {
            }, topic);
            case PAIR -> build(request, (BaseExercise<Pair<String, String>>) persistedExercise, new TypeReference<>() {
            }, topic);
        };
    }

    private static <T> Attempt<T> build(AttemptRequestDTO request, BaseExercise<T> exercise, Class<T> type, Topic topic) {
        T userResponse = request.getResponsesAs(type);
        return new Attempt<>(exercise,
                userResponse, topic);
    }

    private static <T> Attempt<T> build(AttemptRequestDTO request, BaseExercise<T> exercise, TypeReference<T> type, Topic topic) {
        T userResponse = request.getResponsesAs(type);
        return new Attempt<>(exercise,
                userResponse, topic);
    }

    public static <T> Attempt<T> of(BaseExercise<T> exercise,
                                    T userResponse, Topic topic) {
        return new Attempt<>(exercise, userResponse, topic);
    }

    public ExerciseType getExerciseType() {
        return exercise.getType();
    }

    public T getAnswer() {
        return exercise.getAnswer();
    }

    public boolean isAnswerWrong() {
        return !Objects.equals(userResponse, exercise.getAnswer());
    }
}
