package ch.supsi.documentapplication.service;

import ch.supsi.documentapplication.controller.DTO.response.AttemptRequestDTO;
import ch.supsi.documentapplication.controller.DTO.response.AttemptResponseDTO;
import ch.supsi.documentapplication.controller.DTO.response.LessonResponseDTO;
import ch.supsi.documentapplication.dataaccess.*;
import ch.supsi.documentapplication.exception.ApplicationException;
import ch.supsi.documentapplication.exception.InvalidInputException;
import ch.supsi.documentapplication.exception.LessonAttemptsExhaustedException;
import ch.supsi.documentapplication.exception.LessonPrerequisiteNotSatisfiedException;
import ch.supsi.documentapplication.model.Attempt;
import ch.supsi.documentapplication.model.ClientResult;
import ch.supsi.documentapplication.model.PathSettings;
import ch.supsi.documentapplication.model.learn.ExerciseType;
import ch.supsi.documentapplication.model.learn.Lesson;
import ch.supsi.documentapplication.model.learn.Topic;
import ch.supsi.documentapplication.model.learn.exercise.BaseExercise;
import ch.supsi.documentapplication.service.clients.exercise.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LearningService extends Service implements ILearningService {

    private final IDatabasePathStorage databasePathStorage;
    private final IDatabaseLearningStorage databaseLearningStorage;
    private final OpenAIGroupClient groupClient;
    private final OpenAIListClient listClient;
    private final OpenAIPairClient pairClient;
    private final OpenAIQuestionClient questionClient;
    private final DatabaseTopicStorage databaseTopicStorage;
    private final IDatabaseAiUsageStorage aiUsageStorage;
    private final ConcurrentHashMap<Long, ReentrantLock> lessonLocks = new ConcurrentHashMap<>();

    @Autowired
    public LearningService(
            IDatabaseSpaceStorage databaseSpaceStorage,
            DatabasePathStorage databasePathStorage,
            IDatabaseLearningStorage databaseLearningStorage,
            OpenAIGroupClient groupClient,
            OpenAIListClient listClient,
            OpenAIPairClient pairClient,
            OpenAIQuestionClient questionClient,
            DatabaseTopicStorage databaseTopicStorage,
            IDatabaseAiUsageStorage aiUsageStorage) {
        super(databaseSpaceStorage);
        this.databasePathStorage = databasePathStorage;
        this.databaseLearningStorage = databaseLearningStorage;
        this.groupClient = groupClient;
        this.listClient = listClient;
        this.pairClient = pairClient;
        this.questionClient = questionClient;
        this.databaseTopicStorage = databaseTopicStorage;
        this.aiUsageStorage = aiUsageStorage;
    }

    @Override
    public Lesson getLesson(String owner, Long spaceId, Long lessonId) throws ApplicationException {
        return databaseLearningStorage.readLesson(lessonId);
    }

    @Override
    public BaseExercise<?> getNextExercise(String owner, Long spaceId, Long lessonId) throws ApplicationException {
        ReentrantLock lock = lessonLocks.computeIfAbsent(lessonId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            return generateNextExercise(owner, spaceId, lessonId);
        } finally {
            lock.unlock();
        }
    }

    private BaseExercise<?> generateNextExercise(String owner, Long spaceId, Long lessonId)
            throws ApplicationException {
        var space = getSpaceOrThrow(owner, spaceId);
        var path = databasePathStorage.read(space.getId());
        var lesson = getLesson(owner, spaceId, lessonId);
        var prerequisite = path.getPrerequisiteLesson(lesson.getId());

        if (!lesson.canBeStarted(prerequisite)) {
            throw LessonPrerequisiteNotSatisfiedException.lessonPrerequisiteNotSatisfied(
                    lesson.getId(), prerequisite.getId());
        }

        if (lesson.isMastered()) {
            throw LessonPrerequisiteNotSatisfiedException.lessonAlreadyCompleted(lesson.getId());
        }

        if (!lesson.hasAttemptsLeft()) {
            throw LessonAttemptsExhaustedException.lessonAttemptsExhausted();
        }

        if (lesson.hasExerciseInProgress()) {
            return lesson.getExerciseInProgress();
        }

        var settings = path.getSettings();
        var topic = findCurrentTopic(space.getId(), lesson);
        var type = rollExerciseType(settings, topic.getExerciseTypes());

        ClientResult<? extends BaseExercise<?>> exerciseResult =
                switch (type) {
                    case LIST -> listClient.getResult(topic);
                    case GROUP -> groupClient.getResult(topic);
                    case QUESTION -> questionClient.getResult(topic);
                    case PAIR -> pairClient.getResult(topic);
                };
        BaseExercise<?> exercise = exerciseResult.result();
        exercise.setIndex(lesson.getExercisesLength());
        exercise.setTopicId(topic.getId());
        exercise = databaseLearningStorage.createExercise(exercise, lesson.getId());
        Long exerciseId = exercise.getId();
        exerciseResult
                .usages()
                .forEach(usage -> aiUsageStorage.create("exercise", exerciseId, "exercise_generate", usage));

        lesson.addExercise(exercise);
        return exercise;
    }

    private Topic findCurrentTopic(Long spaceId, Lesson lesson) throws ApplicationException {
        return findTopicById(spaceId, lesson.getCurrentTopicId());
    }

    private Topic findTopicById(Long spaceId, Long topicId) throws ApplicationException {
        var topics = databaseTopicStorage.getBySpaceId(spaceId);

        return topics.stream()
                .filter(t -> Objects.equals(t.getId(), topicId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<BaseExercise<?>> getExercises(String owner, Long spaceId, Long nodeId) throws ApplicationException {
        getSpaceOrThrow(owner, spaceId);
        var lesson = getLesson(owner, spaceId, nodeId);

        return lesson.getExercises();
    }

    @Override
    public AttemptResponseDTO submitAttempt(String owner, Long spaceId, Long nodeId, AttemptRequestDTO request)
            throws ApplicationException {
        var space = getSpaceOrThrow(owner, spaceId);
        var lesson = getLesson(owner, spaceId, nodeId);
        var path = databasePathStorage.read(space.getId());
        var chapter = path.getChapters().stream()
                .filter(c -> c.getId().equals(lesson.getChapterId()))
                .findFirst()
                .orElse(null);

        if (!lesson.hasAttemptsLeft()) {
            throw LessonAttemptsExhaustedException.lessonAttemptsExhausted();
        }

        BaseExercise<?> exercise = lesson.getExerciseInProgress();
        Topic topic = findTopicById(space.getId(), exercise.getTopicId());
        Attempt<?> attempt = Attempt.from(request, exercise, topic);
        boolean isAnswerWrong = attempt.isAnswerWrong();

        if (isAnswerWrong) {
            lesson.consumeAttempt();
            Lesson checkPoint = chapter.getLessons().stream()
                    .filter(Lesson::isCheckpoint)
                    .findFirst()
                    .orElse(null);
            if (checkPoint != null
                    && checkPoint.getTopics() != null
                    && !checkPoint.getTopics().contains(topic.getId())) {
                checkPoint.getTopics().add(topic.getId());
                databaseLearningStorage.update(checkPoint);
            }
        }

        databaseLearningStorage.create(attempt, owner, lesson.getId(), exercise.getId());

        lesson.updateMastery(isAnswerWrong);
        lesson.advanceCurrentTopic();
        exercise.setCompleted(true);
        exercise.setCorrect(!isAnswerWrong);
        databaseLearningStorage.updateExercise(exercise);
        databaseLearningStorage.update(lesson);

        return new AttemptResponseDTO(exercise.getId(), !isAnswerWrong, LessonResponseDTO.from(lesson));
    }

    @Override
    public void deleteExercises(String owner, Long spaceId, Long lessonId) {
        ReentrantLock lock = lessonLocks.computeIfAbsent(lessonId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            getSpaceOrThrow(owner, spaceId);
            var lesson = getLesson(owner, spaceId, lessonId);

            if (lesson.hasAttemptsLeft()) {
                throw InvalidInputException.attemptsNotExhausted();
            }
            lesson.reset();
            databaseLearningStorage.update(lesson);

            databaseLearningStorage.deleteExercises(lesson.getId());
        } finally {
            lock.unlock();
        }
    }

    private ExerciseType rollExerciseType(PathSettings settings, List<ExerciseType> topicTypes) {
        List<ExerciseType> pool = topicTypes == null || topicTypes.isEmpty()
                ? List.of(ExerciseType.QUESTION)
                : topicTypes.stream().filter(Objects::nonNull).distinct().toList();

        if (pool.isEmpty()) {
            pool = List.of(ExerciseType.QUESTION);
        }

        var weights = new LinkedHashMap<ExerciseType, Integer>();
        for (ExerciseType type : pool) {
            weights.put(type, percentageFor(settings, type));
        }

        int total = weights.values().stream().mapToInt(v -> v == null ? 0 : v).sum();
        if (total <= 0) {
            return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        }

        return pickWeighted(weights, ThreadLocalRandom.current().nextInt(total));
    }

    private static Integer percentageFor(PathSettings settings, ExerciseType type) {
        return switch (type) {
            case QUESTION -> settings.getQuestionPercentage();
            case LIST -> settings.getListPercentage();
            case GROUP -> settings.getGroupPercentage();
            case PAIR -> settings.getPairPercentage();
        };
    }

    public static ExerciseType pickWeighted(LinkedHashMap<ExerciseType, Integer> weights, int roll) {
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
