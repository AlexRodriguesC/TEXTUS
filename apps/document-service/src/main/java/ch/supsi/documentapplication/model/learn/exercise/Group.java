package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonTypeName("GROUP")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "exerciseType", defaultImpl = Group.class)
@JsonClassDescription(
        "A grouping exercise where the user sorts a set of strings into exactly two mutually exclusive, collectively exhaustive labeled groups.")
public class Group extends BaseExercise<Map<String, List<String>>> {

    @Override
    public boolean isAnswerCorrect(Map<String, List<String>> userResponse) {
        if (userResponse == null) {
            return false;
        }
        if (this.answer == null) {
            return userResponse.isEmpty();
        }
        if (!this.answer.keySet().equals(userResponse.keySet())) {
            return false;
        }
        for (String key : this.answer.keySet()) {
            List<String> expected = this.answer.get(key);
            List<String> actual = userResponse.get(key);
            if (expected == null && actual == null) {
                continue;
            }
            if (expected == null || actual == null) {
                return false;
            }
            if (expected.size() != actual.size()) {
                return false;
            }
            List<String> sortedExpected = new ArrayList<>(expected);
            Collections.sort(sortedExpected);
            List<String> sortedActual = new ArrayList<>(actual);
            Collections.sort(sortedActual);
            if (!sortedExpected.equals(sortedActual)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ExerciseType getType() {
        return ExerciseType.GROUP;
    }

    @Override
    public double getGuessProbability() {
        return 0.05;
    }
}
