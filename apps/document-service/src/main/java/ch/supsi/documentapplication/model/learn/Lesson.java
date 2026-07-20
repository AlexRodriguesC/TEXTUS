package ch.supsi.documentapplication.model.learn;

import ch.supsi.documentapplication.model.LessonStatus;
import ch.supsi.documentapplication.model.learn.exercise.BaseExercise;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Lesson {

    private static final double P_T = 0.03;
    private static final double P_S = 0.10;
    private static final int MAX_ATTEMPTS = 5;
    public static final double INITIAL_MASTERY = 0.10;

    private Long id;
    private Long chapterId;
    private final Integer index;
    private LessonType type;
    private List<Long> topics;
    private List<BaseExercise<?>> exercises;

    @Setter
    private Long currentTopic = 0L;

    @Setter
    private double mastery = INITIAL_MASTERY;

    @Setter
    private int attemptsLeft = MAX_ATTEMPTS;

    public Lesson(
            Long id,
            Long chapterId,
            Integer index,
            LessonType type,
            List<Long> topics,
            Long currentTopic,
            int attemptsLeft,
            double mastery) {
        this.id = id;
        this.chapterId = chapterId;
        this.index = index;
        this.type = type;
        this.topics = topics;
        this.currentTopic = currentTopic;
        this.attemptsLeft = attemptsLeft;
        this.mastery = mastery;
        this.exercises = new ArrayList<>();
    }

    public Lesson(LessonType type, Integer index, List<Long> topics) {
        this.type = type;
        this.index = index;
        this.topics = topics;
    }

    public boolean isCheckpoint() {
        return type == LessonType.CHECKPOINT;
    }

    public boolean canBeStarted(Lesson prerequisite) {
        return prerequisite == null || prerequisite.isMastered();
    }

    public int getMaxAttempts() {
        return MAX_ATTEMPTS;
    }

    public Long getCurrentTopicId() {
        return topics.get(Math.toIntExact(currentTopic));
    }

    public Long getCurrentTopic() {
        return currentTopic;
    }

    public void consumeAttempt() {
        attemptsLeft--;
    }

    public Integer getExercisesLength() {
        return exercises == null ? 0 : exercises.size();
    }

    public void addExercise(BaseExercise<?> exercise) {
        exercises.add(exercise);
    }

    public LessonStatus getStatus() {
        if (isMastered()) {
            return LessonStatus.MASTERED;
        } else if (attemptsLeft <= 0) {
            return LessonStatus.EXHAUSTED;
        }
        return LessonStatus.IN_PROGRESS;
    }

    public boolean hasAttemptsLeft() {
        return attemptsLeft > 0;
    }

    public void advanceCurrentTopic() {
        if (topics == null || topics.isEmpty()) {
            return;
        }
        currentTopic = (currentTopic + 1) % topics.size();
    }

    public boolean isMastered() {
        return mastery >= 0.95;
    }

    public boolean hasExerciseInProgress() {
        return exercises != null && exercises.stream().anyMatch(e -> !e.isCompleted());
    }

    public BaseExercise<?> getExerciseInProgress() {
        return exercises.stream().filter(e -> !e.isCompleted()).findFirst().orElse(null);
    }

    public void updateMastery(boolean answerWrong) {
        BaseExercise<?> exercise = getExerciseInProgress();
        double guessProbability = exercise.getGuessProbability();

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

    public void reset() {
        this.mastery = INITIAL_MASTERY;
        this.attemptsLeft = MAX_ATTEMPTS;
    }
}
