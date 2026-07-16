package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("LIST")
public class List extends BaseExercise<ListAnswer> {

    @Override
    public boolean isAnswerCorrect(ListAnswer userResponse) {
        if (this.answer.getList().size() != userResponse.getList().size()) {
            return false;
        }

        for (int i = 0; i < this.answer.getList().size(); i++) {
            if (this.answer.getList().get(i).equals(userResponse.getList().get(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ExerciseType getType() {
        return ExerciseType.LIST;
    }

    @Override
    public double getGuessProbability() {
        return 0.05;
    }
}
