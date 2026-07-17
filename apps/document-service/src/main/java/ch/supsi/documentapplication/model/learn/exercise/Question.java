package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.Collections;
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

    public List<String> getShuffledDistractors() {
        List<String> distractor = new ArrayList<>(this.distractor);
        distractor.add(this.answer);
        Collections.shuffle(distractor);
        return distractor;
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
