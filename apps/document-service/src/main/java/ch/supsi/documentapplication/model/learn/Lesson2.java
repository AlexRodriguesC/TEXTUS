package ch.supsi.documentapplication.model.learn;

import java.util.List;

import ch.supsi.documentapplication.model.LessonStatus;
import ch.supsi.documentapplication.model.learn.exercise.BaseExercise;
import ch.supsi.documentapplication.model.learn.exercise.Exercise;
import lombok.Getter;

@Getter
public class Lesson2 {

    private static final double P_T = 0.03;
    private static final double P_S = 0.10;

    private Long id;
    private final Integer index;
    private LessonType type;
    private List<Long> topics;
    private List<Exercise> exercises;
    private Long currentTopic;
    private LessonStatus status;
    private double mastery = 0.10;
    private int attemptsLeft = 10;


    public Lesson2(Long id, LessonType type, Integer index, List<Long> topics) {
        this.id = id;
        this.type = type;
        this.index = index;
        this.topics = topics;
    }

    public Lesson2(LessonType type, Integer index, List<Long> topics) {
        this.type = type;
        this.index = index;
        this.topics = topics;
    }

    public void consumeAttempt() {
        attemptsLeft--;
    }

    public Integer getExercisesLength() {
        return exercises.size();
    }

    public void addExercise(Exercise exercise) {
        exercises.add(exercise);
    }

    public void advanceCurrentTopic() {
        if (topics == null || topics.isEmpty()) {
            return;
        }
        int index = topics.indexOf(currentTopic);
        if (index < 0) {
            // currentTopic not found in topics; fallback to first topic
            currentTopic = topics.getFirst();
        } else {
            int nextIndex = (index + 1) % topics.size();
            currentTopic = topics.get(nextIndex);
        }
    }

    public boolean isMastered() {
        return mastery >= 0.95;
    }

    public boolean hasExerciseInProgress() {
        return exercises != null && exercises.stream().anyMatch(e -> !e.isCompleted());
    }

    public Exercise getExerciseInProgress() {
        return exercises.stream().filter(e -> !e.isCompleted()).findFirst().orElse(null);
    }

    public void updateMastery(boolean answerWrong) {
        Exercise exercise = getExerciseInProgress();
        BaseExercise<?> baseExercise = exercise.getBaseExercise();
        double guessProbability = baseExercise.getGuessProbability();

        double increment;
        if (answerWrong) {
            double probWrong = mastery * P_S;
            double wrongCases = probWrong + ((1 - mastery) * (1 - guessProbability));
            increment = probWrong / wrongCases;
        } else {
            double probCorrect = mastery * (1 - P_S);
            double correctCases = probCorrect + ((1 - mastery) * guessProbability);
            increment = probCorrect / correctCases;
        }
        mastery = increment + (1 - increment) * P_T;
        mastery = Math.max(0, mastery);
        mastery = Math.min(1, mastery);
    }
}
