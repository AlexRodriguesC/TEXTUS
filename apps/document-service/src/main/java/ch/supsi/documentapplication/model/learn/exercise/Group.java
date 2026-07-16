package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

@JsonTypeName("GROUP")
public class Group extends BaseExercise<List<GroupAnswer>> {

    @Override
    public ExerciseType getType() {
        return ExerciseType.GROUP;
    }

    @Override
    public double getGuessProbability() {
        return 0.05;
    }
}
