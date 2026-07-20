package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("LIST")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "exerciseType", defaultImpl = List.class)
@JsonClassDescription(
        "An ordering exercise where the user arranges a set of elements into their single correct sequence.")
public class List extends BaseExercise<java.util.List<String>> {

    @Override
    public boolean isAnswerCorrect(java.util.List<String> userResponse) {
        if (userResponse == null) {
            return false;
        }
        if (this.answer == null) {
            return userResponse.isEmpty();
        }
        if (this.answer.size() != userResponse.size()) {
            return false;
        }

        return answer.equals(userResponse);
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
