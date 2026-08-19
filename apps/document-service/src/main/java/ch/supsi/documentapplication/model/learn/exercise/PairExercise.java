package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@JsonTypeName("PAIR")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "exerciseType", defaultImpl = PairExercise.class)
@JsonClassDescription(
        "A matching exercise where the user associates each left element with its correct right counterpart, expressed as a list of left/right pairs.")
public class PairExercise extends BaseExercise<List<LeftRight>> {

    @Override
    public boolean isAnswerCorrect(List<LeftRight> userResponse) {
        if (userResponse == null) {
            return false;
        }
        if (this.answer == null) {
            return userResponse.isEmpty();
        }
        if (this.answer.size() != userResponse.size()) {
            return false;
        }
        List<LeftRight> sortedExpected = new ArrayList<>(this.answer);
        List<LeftRight> sortedActual = new ArrayList<>(userResponse);
        Comparator<LeftRight> pairComparator =
                Comparator.comparing(LeftRight::left).thenComparing(LeftRight::right);
        sortedExpected.sort(pairComparator);
        sortedActual.sort(pairComparator);
        return sortedExpected.equals(sortedActual);
    }

    @Override
    public ExerciseType getType() {
        return ExerciseType.PAIR;
    }

    @Override
    public double getGuessProbability() {
        return 0.05;
    }
}
