package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Exercise {

    private Long id;
    private Integer index;
    private Long topicId;
    private boolean completed;
    private boolean correct;
    private ExerciseType type;
    private Object exercise;
    private BaseExercise<?> baseExercise;

    public Exercise(Integer index, Long topicId, boolean completed, ExerciseType type, Object exercise) {
        this.index = index;
        this.topicId = topicId;
        this.completed = completed;
        this.type = type;
        this.exercise = exercise;
    }

    public void complete(boolean correct) {
        completed = true;
        this.correct = correct;
    }
}
