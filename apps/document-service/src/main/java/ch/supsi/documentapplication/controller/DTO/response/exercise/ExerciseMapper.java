package ch.supsi.documentapplication.controller.DTO.response.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import ch.supsi.documentapplication.model.learn.exercise.BaseExercise;
import ch.supsi.documentapplication.model.learn.exercise.Group;
import ch.supsi.documentapplication.model.learn.exercise.PairExercise;
import ch.supsi.documentapplication.model.learn.exercise.Question;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExerciseMapper {

    public ExerciseDTO toDto(BaseExercise<?> exercise) {
        return switch (exercise) {
            case Question q -> new QuestionResponseDTO(
                    q.getId(), q.getTopicId(),
                    q.getRequest(), q.isCompleted(), ExerciseType.QUESTION, q.getAnswer(), q.getDistractor());

            case ch.supsi.documentapplication.model.learn.exercise.List l -> new ListResponseDTO(
                    l.getId(), l.getTopicId(),
                    l.getRequest(), l.isCompleted(), ExerciseType.LIST, l.getAnswer());

            case Group g -> new GroupResponseDTO(
                    g.getId(), g.getTopicId(),
                    g.getRequest(), g.isCompleted(), ExerciseType.GROUP, g.getAnswer());

            case PairExercise p -> new PairResponseDTO(
                    p.getId(),
                    p.getTopicId(),
                    p.getRequest(), p.isCompleted(), ExerciseType.PAIR,
                    p.getAnswer());

            default -> throw new IllegalArgumentException("Unsupported exercise type: " + exercise.getClass());
        };
    }

    public List<ExerciseDTO> toDtoList(List<BaseExercise<?>> exercises) {
        return exercises.stream().map(this::toDto).toList();
    }

}
