package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.controller.DTO.response.AttemptRequestDTO;
import ch.supsi.documentapplication.controller.DTO.response.AttemptResponseDTO;
import ch.supsi.documentapplication.dataaccess.*;
import ch.supsi.documentapplication.exception.ApplicationException;
import ch.supsi.documentapplication.model.Attempt;
import ch.supsi.documentapplication.model.PathSettings;
import ch.supsi.documentapplication.model.learn.ExerciseType;
import ch.supsi.documentapplication.model.learn.Lesson2;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.model.learn.exercise.Exercise;
import ch.supsi.documentapplication.service.clients.exercise.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class LearningService extends Service implements ILearningService {

    private final IDatabasePathStorage databasePathStorage;
    private final IDatabaseLearningStorage databaseLearningStorage;
    private final OpenAIGroupClient groupClient;
    private final OpenAIListClient listClient;
    private final OpenAIPairClient pairClient;
    private final OpenAIQuestionClient questionClient;
    private final DatabaseTopicStorage databaseTopicStorage;
    private final OpenAIExerciseFeedbackClient openAIExerciseFeedbackClient;

    @Autowired
    public LearningService(IDatabaseSpaceStorage databaseSpaceStorage, DatabasePathStorage databasePathStorage, IDatabaseLearningStorage databaseLearningStorage, OpenAIGroupClient groupClient, OpenAIListClient listClient, OpenAIPairClient pairClient, OpenAIQuestionClient questionClient, DatabaseTopicStorage databaseTopicStorage, OpenAIExerciseFeedbackClient openAIExerciseFeedbackClient) {
        super(databaseSpaceStorage);
        this.databasePathStorage = databasePathStorage;
        this.databaseLearningStorage = databaseLearningStorage;
        this.groupClient = groupClient;
        this.listClient = listClient;
        this.pairClient = pairClient;
        this.questionClient = questionClient;
        this.databaseTopicStorage = databaseTopicStorage;
        this.openAIExerciseFeedbackClient = openAIExerciseFeedbackClient;
    }

    @Override
    public Lesson2 getLesson2(String owner, Long spaceId, Long lessonId) throws ApplicationException {
        return databaseLearningStorage.readLesson(lessonId);
    }

    @Override
    public Exercise getNextExercise(String owner, Long spaceId, Long nodeId) throws ApplicationException {
        var space = getSpaceOrThrow(owner, spaceId);
        var path = databasePathStorage.read(space.getId());
        var lesson = getLesson2(owner, spaceId, nodeId);

        if (lesson.hasExerciseInProgress()) {
            return lesson.getExerciseInProgress();
        }

        var settings = path.getSettings();
        var type = rollExerciseType(settings);
        var topic = findCurrentTopic(space.getId(), lesson);

        var exerciseContent = switch (type) {
            case LIST -> listClient.getResult(topic);
            case GROUP -> groupClient.getResult(topic);
            case QUESTION -> questionClient.getResult(topic);
            case PAIR -> pairClient.getResult(topic);
        };

        var exercise = new Exercise(lesson.getExercisesLength(), topic.getId(), false, type, exerciseContent);
        exercise = databaseLearningStorage.createExercise(exercise);

        lesson.advanceCurrentTopic();
        lesson.addExercise(exercise);
        return exercise;
    }

    private Topic findCurrentTopic(Long spaceId, Lesson2 lesson) throws ApplicationException {
        var topics = databaseTopicStorage.getBySpaceId(spaceId);
        return topics.stream()
                .filter(t -> Objects.equals(t.getId(), lesson.getCurrentTopic()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Exercise> getExercises(String owner, Long spaceId, Long nodeId) throws ApplicationException {
        getSpaceOrThrow(owner, spaceId);
        var lesson = getLesson2(owner, spaceId, nodeId);

        return lesson.getExercises();
    }

    @Override
    public AttemptResponseDTO submitAttempt(String owner, Long spaceId, Long nodeId, AttemptRequestDTO request) throws ApplicationException {
        var space = getSpaceOrThrow(owner, spaceId);
        var lesson = getLesson2(owner, spaceId, nodeId);
        Exercise exercise = lesson.getExerciseInProgress();
        Topic topic = findCurrentTopic(space.getId(), lesson);
        Attempt<?> attempt = Attempt.from(request, exercise.getBaseExercise(), topic);
        boolean isAnswerWrong = attempt.isAnswerWrong();

        if (isAnswerWrong) {
            String feedback = openAIExerciseFeedbackClient.getResult(attempt);
            attempt.setFeedback(feedback);
        }

        databaseLearningStorage.create(attempt);

        exercise.complete(!isAnswerWrong);
        databaseLearningStorage.updateExercise(exercise);

        lesson.updateMastery(isAnswerWrong);
        lesson.consumeAttempt();
        databaseLearningStorage.update(lesson);

        return null;
    }

    private ExerciseType rollExerciseType(PathSettings settings) {
        var weights = new LinkedHashMap<ExerciseType, Integer>();
        weights.put(ExerciseType.QUESTION, settings.getQuestionPercentage());
        weights.put(ExerciseType.LIST, settings.getListPercentage());
        weights.put(ExerciseType.GROUP, settings.getGroupPercentage());
        weights.put(ExerciseType.PAIR, settings.getPairPercentage());

        int total = weights.values().stream()
                .mapToInt(v -> v == null ? 0 : v)
                .sum();

        if (total <= 0) {
            throw new IllegalStateException("PathSettings percentages must sum to a positive value");
        }

        int roll = ThreadLocalRandom.current().nextInt(total);
        int cumulative = 0;

        for (var entry : weights.entrySet()) {
            int weight = entry.getValue() == null ? 0 : entry.getValue();
            cumulative += weight;
            if (roll < cumulative) {
                return entry.getKey();
            }
        }

        throw new IllegalStateException("Unreachable: roll did not match any exercise type");
    }
}
