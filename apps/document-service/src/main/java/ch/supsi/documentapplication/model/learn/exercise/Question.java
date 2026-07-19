package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@JsonTypeName("QUESTION")
public class Question extends BaseExercise<String> {

    @Getter
    @Setter
    private List<String> distractor;

    @Override
    public boolean isAnswerCorrect(String userResponse) {
        return this.answer.equals(userResponse);
    }

    @Override
    public ExerciseType getType() {
        return ExerciseType.QUESTION;
    }

    @Override
    public double getGuessProbability() {
        return 1.0 / (1 + distractor.size());
    }
}
