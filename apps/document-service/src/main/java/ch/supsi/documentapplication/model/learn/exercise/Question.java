package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

import java.util.List;

@JsonTypeName("QUESTION")
public class Question extends BaseExercise<String> {

    @Getter
    private List<String> distractor;

    @Override
    public ExerciseType getType() {
        return ExerciseType.QUESTION;
    }

    @Override
    public double getGuessProbability() {
        return 1.0 / (1 + distractor.size());
    }
}
