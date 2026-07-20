package ch.supsi.documentapplication.model.learn.exercise;

import ch.supsi.documentapplication.model.learn.ExerciseType;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@JsonTypeName("QUESTION")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "exerciseType", defaultImpl = Question.class)
@JsonClassDescription(
        "A multiple-choice or true/false question exercise with one correct answer and one or more incorrect distractors.")
public class Question extends BaseExercise<String> {

    @Getter
    @Setter
    @JsonPropertyDescription(
            "Incorrect but plausible alternative answers, in the same format as the correct answer (e.g. all boolean-as-string for true/false, all plain text otherwise). Never includes the correct answer.")
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
