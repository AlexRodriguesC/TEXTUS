package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.Collections;

@JsonTypeName("LIST")
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
        // Sort both lists to ignore order
        java.util.List<String> sortedExpected = new ArrayList<>(this.answer);
        Collections.sort(sortedExpected);
        java.util.List<String> sortedActual = new ArrayList<>(userResponse);
        Collections.sort(sortedActual);
        return sortedExpected.equals(sortedActual);
    }

    public java.util.List<String> getShuffledList() {
        java.util.List<String> original = new ArrayList<>(this.answer);
        Collections.shuffle(original);
        return original;
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
