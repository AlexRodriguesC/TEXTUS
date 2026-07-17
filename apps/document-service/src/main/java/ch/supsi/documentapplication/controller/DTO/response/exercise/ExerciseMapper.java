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
                    q.getId(),
                    q.getRequest(), q.isCompleted(), ExerciseType.QUESTION, q.getShuffledDistractors());

            case ch.supsi.documentapplication.model.learn.exercise.List l -> new ListResponseDTO(
                    l.getId(),
                    l.getRequest(), l.isCompleted(), ExerciseType.LIST, l.getShuffledList());

            case Group g -> new GroupResponseDTO(
                    g.getId(),
                    g.getRequest(), g.isCompleted(), ExerciseType.GROUP, g.getLabels(), g.getShuffledValues());

            case PairExercise p -> {

                var shuffledPairs = p.getShuffledPairs();

                yield new PairResponseDTO(
                        p.getId(),
                        p.getRequest(), p.isCompleted(), ExerciseType.PAIR,
                        shuffledPairs.getLeft(), shuffledPairs.getRight());
            }

            default -> throw new IllegalArgumentException("Unsupported exercise type: " + exercise.getClass());
        };
    }

    public List<ExerciseDTO> toDtoList(List<BaseExercise<?>> exercises) {
        return exercises.stream().map(this::toDto).toList();
    }

}
