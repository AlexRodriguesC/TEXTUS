package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

@JsonTypeName("PAIR")
public class PairExercise extends BaseExercise<List<org.apache.commons.lang3.tuple.Pair<String, String>>> {

    @Override
    public boolean isAnswerCorrect(List<org.apache.commons.lang3.tuple.Pair<String, String>> userResponse) {
        if (userResponse == null) {
            return false;
        }
        if (this.answer == null) {
            return userResponse.isEmpty();
        }
        if (this.answer.size() != userResponse.size()) {
            return false;
        }
        // Sort both lists to compare pairs ignoring order
        List<Pair<String, String>> sortedExpected = new ArrayList<>(this.answer);
        List<Pair<String, String>> sortedActual = new ArrayList<>(userResponse);
        Comparator<org.apache.commons.lang3.tuple.Pair<String, String>> pairComparator =
                Comparator.comparing(Pair<String, String>::getLeft).thenComparing(Pair::getRight);
        sortedExpected.sort(pairComparator);
        sortedActual.sort(pairComparator);
        return sortedExpected.equals(sortedActual);
    }

    public Pair<List<String>, List<String>> getShuffledPairs() {
        List<String> left =
                new ArrayList<>(this.answer.stream().map(Pair::getLeft).toList());
        List<String> right =
                new ArrayList<>(this.answer.stream().map(Pair::getRight).toList());
        Collections.shuffle(left);
        Collections.shuffle(right);
        return Pair.of(left, right);
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
