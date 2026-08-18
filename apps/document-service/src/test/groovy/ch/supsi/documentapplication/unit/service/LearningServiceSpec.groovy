package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.controller.DTO.response.AttemptRequestDTO
import ch.supsi.documentapplication.dataaccess.DatabasePathStorage
import ch.supsi.documentapplication.dataaccess.DatabaseTopicStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseAiUsageStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseLearningStorage
import ch.supsi.documentapplication.dataaccess.IDatabaseSpaceStorage
import ch.supsi.documentapplication.exception.InvalidInputException
import ch.supsi.documentapplication.exception.LessonAttemptsExhaustedException
import ch.supsi.documentapplication.exception.LessonPrerequisiteNotSatisfiedException
import ch.supsi.documentapplication.model.AiUsage
import ch.supsi.documentapplication.model.ClientResult
import ch.supsi.documentapplication.model.PathSettings
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.model.learn.Chapter
import ch.supsi.documentapplication.model.learn.ExerciseType
import ch.supsi.documentapplication.model.learn.LearningPath
import ch.supsi.documentapplication.model.learn.Lesson
import ch.supsi.documentapplication.model.learn.LessonType
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.model.learn.exercise.BaseExercise
import ch.supsi.documentapplication.model.learn.exercise.Question
import ch.supsi.documentapplication.service.LearningService
import ch.supsi.documentapplication.service.clients.exercise.OpenAIGroupClient
import ch.supsi.documentapplication.service.clients.exercise.OpenAIListClient
import ch.supsi.documentapplication.service.clients.exercise.OpenAIPairClient
import ch.supsi.documentapplication.service.clients.exercise.OpenAIQuestionClient
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class LearningServiceSpec extends Specification {

    def spaceStorage = Mock(IDatabaseSpaceStorage)
    def pathStorage = Mock(DatabasePathStorage)
    def learningStorage = Mock(IDatabaseLearningStorage)
    def groupClient = Mock(OpenAIGroupClient)
    def listClient = Mock(OpenAIListClient)
    def pairClient = Mock(OpenAIPairClient)
    def questionClient = Mock(OpenAIQuestionClient)
    def topicStorage = Mock(DatabaseTopicStorage)
    def aiUsageStorage = Mock(IDatabaseAiUsageStorage)

    def service = new LearningService(
            spaceStorage, pathStorage, learningStorage,
            groupClient, listClient, pairClient, questionClient,
            topicStorage, aiUsageStorage)

    def setupSpace() {
        spaceStorage.read(10L) >> Optional.of(new Space(10L, "alice", "", "", ""))
    }

    static def buildLesson(Long id = 1L, double mastery = 0.10d, int attemptsLeft = 5, List<Long> topics = [100L]) {
        new Lesson(id, 1L, 0, LessonType.LESSON, topics, 0L, attemptsLeft, mastery)
    }

    static def buildPath(Lesson lesson, Lesson prerequisite = null) {
        def lessons = prerequisite != null ? [prerequisite, lesson] : [lesson]
        def chapter = new Chapter(1L, 0, "Chapter", "desc", null)
        chapter.setLessons(lessons)
        new LearningPath(99L, new PathSettings(100, 0, 0, 0), [chapter])
    }

    def "should throw when prerequisite is not mastered"() {
        given:
        setupSpace()
        def prerequisite = buildLesson(1L, 0.50d)
        def lesson = new Lesson(2L, 1L, 1, LessonType.LESSON, [100L], 0L, 5, 0.10d)
        def path = buildPath(lesson, prerequisite)

        when:
        service.getNextExercise("alice", 10L, 2L)

        then:
        1 * learningStorage.readLesson(2L) >> lesson
        1 * pathStorage.read(10L) >> path
        thrown(LessonPrerequisiteNotSatisfiedException)
    }

    def "should throw when lesson is already mastered"() {
        given:
        setupSpace()
        def lesson = buildLesson(1L, 0.96d)
        def path = buildPath(lesson)

        when:
        service.getNextExercise("alice", 10L, 1L)

        then:
        1 * learningStorage.readLesson(1L) >> lesson
        1 * pathStorage.read(10L) >> path
        thrown(LessonPrerequisiteNotSatisfiedException)
    }

    def "should return exercise in progress when one exists"() {
        given:
        setupSpace()
        def lesson = buildLesson()
        def exercise = new Question()
        exercise.setCompleted(false)
        lesson.addExercise(exercise)
        def path = buildPath(lesson)

        when:
        def result = service.getNextExercise("alice", 10L, 1L)

        then:
        1 * learningStorage.readLesson(1L) >> lesson
        1 * pathStorage.read(10L) >> path
        result == exercise
        0 * questionClient._
    }

    def "should generate question exercise when settings roll question type"() {
        given:
        setupSpace()
        def lesson = buildLesson()
        def path = buildPath(lesson)
        def topic = new Topic(100L, "a.pdf", 1, 1, "title", "summary", [])
        def generated = new Question()
        generated.setRequest("Q?")
        generated.setAnswer("A")

        when:
        def result = service.getNextExercise("alice", 10L, 1L)

        then:
        1 * learningStorage.readLesson(1L) >> lesson
        1 * pathStorage.read(10L) >> path
        1 * topicStorage.getBySpaceId(10L) >> [topic]
        1 * questionClient.getResult(topic) >> new ClientResult<>(generated, [new AiUsage(1, 2, "p", "m")])
        1 * learningStorage.createExercise(_ as Question, 1L) >> { Question q, Long id ->
            q.setId(42L)
            return q
        }
        1 * aiUsageStorage.create("exercise", 42L, "exercise_generate", _ as AiUsage)
        result.id == 42L
    }

    def "should generate only once when two requests overlap"() {
        given:
        def lesson = buildLesson()
        def path = buildPath(lesson)
        def topic = new Topic(100L, "a.pdf", 1, 1, "title", "summary", [])
        def generated = new Question()
        generated.setRequest("Q?")
        generated.setAnswer("A")

        def generationStarted = new CountDownLatch(1)
        def generationHold = new CountDownLatch(1)
        def generateCalls = new AtomicInteger(0)
        def createCalls = new AtomicInteger(0)

        def localSpaceStorage = [
                read: { Long ignored -> Optional.of(new Space(10L, "alice", "", "", "")) }
        ] as IDatabaseSpaceStorage
        def localPathStorage = new DatabasePathStorage(null) {
            @Override
            LearningPath read(Long spaceId) {
                return path
            }
        }
        def localLearningStorage = [
                readLesson: { Long ignored -> lesson },
                createExercise: { BaseExercise<?> exercise, Long ignored ->
                    createCalls.incrementAndGet()
                    exercise.setId(42L)
                    return exercise
                }
        ] as IDatabaseLearningStorage
        def localTopicStorage = new DatabaseTopicStorage(null) {
            @Override
            List<Topic> getBySpaceId(Long spaceId) {
                return [topic]
            }
        }
        def localQuestionClient = new OpenAIQuestionClient(null, null, "", null, null) {
            @Override
            ClientResult<Question> getResult(Topic input) {
                generateCalls.incrementAndGet()
                generationStarted.countDown()
                assert generationHold.await(2, TimeUnit.SECONDS)
                return new ClientResult<>(generated, [new AiUsage(1, 2, "p", "m")])
            }
        }
        def localAiUsageStorage = [
                create: { String type, Long entityId, String operation, AiUsage usage -> }
        ] as IDatabaseAiUsageStorage
        def overlappingService = new LearningService(
                localSpaceStorage,
                localPathStorage,
                localLearningStorage,
                Mock(OpenAIGroupClient),
                Mock(OpenAIListClient),
                Mock(OpenAIPairClient),
                localQuestionClient,
                localTopicStorage,
                localAiUsageStorage)

        def first = new AtomicReference()
        def second = new AtomicReference()
        def errors = new AtomicReference<Throwable>()

        when:
        def t1 = Thread.start {
            try {
                first.set(overlappingService.getNextExercise("alice", 10L, 1L))
            } catch (Throwable t) {
                errors.compareAndSet(null, t)
            }
        }
        assert generationStarted.await(2, TimeUnit.SECONDS)
        def t2 = Thread.start {
            try {
                second.set(overlappingService.getNextExercise("alice", 10L, 1L))
            } catch (Throwable t) {
                errors.compareAndSet(null, t)
            }
        }
        Thread.sleep(50)
        generationHold.countDown()
        t1.join(2000)
        t2.join(2000)

        then:
        errors.get() == null
        generateCalls.get() == 1
        createCalls.get() == 1
        first.get().id == 42L
        second.get().id == 42L
        first.get().is(second.get())
    }

    def "should throw when attempts are exhausted on getNextExercise"() {
        given:
        setupSpace()
        def lesson = buildLesson(1L, 0.10d, 0)
        def path = buildPath(lesson)

        when:
        service.getNextExercise("alice", 10L, 1L)

        then:
        1 * learningStorage.readLesson(1L) >> lesson
        1 * pathStorage.read(10L) >> path
        thrown(LessonAttemptsExhaustedException)
        0 * questionClient._
    }

    def "should throw when attempts are exhausted on submit"() {
        given:
        setupSpace()
        def lesson = buildLesson(1L, 0.10d, 0)
        def path = buildPath(lesson)

        when:
        service.submitAttempt("alice", 10L, 1L, new AttemptRequestDTO(ExerciseType.QUESTION, "a1", 1L, "4"))

        then:
        1 * learningStorage.readLesson(1L) >> lesson
        1 * pathStorage.read(10L) >> path
        thrown(LessonAttemptsExhaustedException)
    }

    def "should throw when deleting exercises before attempts exhausted"() {
        given:
        setupSpace()
        def lesson = buildLesson()

        when:
        service.deleteExercises("alice", 10L, 1L)

        then:
        1 * learningStorage.readLesson(1L) >> lesson
        thrown(InvalidInputException)
        0 * learningStorage.deleteExercises(_)
    }

    def "should delete exercises when attempts are exhausted"() {
        given:
        setupSpace()
        def lesson = buildLesson(1L, 0.10d, 0)

        when:
        service.deleteExercises("alice", 10L, 1L)

        then:
        1 * learningStorage.readLesson(1L) >> lesson
        1 * learningStorage.update(lesson)
        1 * learningStorage.deleteExercises(1L)
    }

    def "should add exercise topic to checkpoint on wrong attempt"() {
        given:
        setupSpace()
        def checkpoint = new Lesson(2L, 1L, 1, LessonType.CHECKPOINT, [], 0L, 5, 0.10d)
        def lesson = buildLesson(1L)
        def exercise = new Question()
        exercise.setId(10L)
        exercise.setTopicId(100L)
        exercise.setAnswer("4")
        exercise.setDistractor(["3", "5"])
        exercise.setCompleted(false)
        lesson.addExercise(exercise)
        def chapter = new Chapter(1L, 0, "Chapter", "desc", null)
        chapter.setLessons([lesson, checkpoint])
        def path = new LearningPath(99L, new PathSettings(100, 0, 0, 0), [chapter])
        def topic = new Topic(100L, "a.pdf", 1, 1, "title", "summary", [])

        when:
        service.submitAttempt("alice", 10L, 1L, new AttemptRequestDTO(ExerciseType.QUESTION, "a1", 10L, "3"))

        then:
        1 * learningStorage.readLesson(1L) >> lesson
        1 * pathStorage.read(10L) >> path
        1 * topicStorage.getBySpaceId(10L) >> [topic]
        1 * learningStorage.create(_, "alice", 1L, 10L)
        1 * learningStorage.updateExercise(_)
        1 * learningStorage.update(lesson)
        1 * learningStorage.update({ Lesson l -> l.id == 2L && l.topics == [100L] })
        checkpoint.topics == [100L]
    }

    def "should not update checkpoint when topic already present on wrong attempt"() {
        given:
        setupSpace()
        def checkpoint = new Lesson(2L, 1L, 1, LessonType.CHECKPOINT, [100L], 0L, 5, 0.10d)
        def lesson = buildLesson(1L)
        def exercise = new Question()
        exercise.setId(10L)
        exercise.setTopicId(100L)
        exercise.setAnswer("4")
        exercise.setDistractor(["3", "5"])
        exercise.setCompleted(false)
        lesson.addExercise(exercise)
        def chapter = new Chapter(1L, 0, "Chapter", "desc", null)
        chapter.setLessons([lesson, checkpoint])
        def path = new LearningPath(99L, new PathSettings(100, 0, 0, 0), [chapter])
        def topic = new Topic(100L, "a.pdf", 1, 1, "title", "summary", [])

        when:
        service.submitAttempt("alice", 10L, 1L, new AttemptRequestDTO(ExerciseType.QUESTION, "a1", 10L, "3"))

        then:
        1 * learningStorage.readLesson(1L) >> lesson
        1 * pathStorage.read(10L) >> path
        1 * topicStorage.getBySpaceId(10L) >> [topic]
        1 * learningStorage.create(_, "alice", 1L, 10L)
        1 * learningStorage.updateExercise(_)
        1 * learningStorage.update(lesson)
        0 * learningStorage.update({ Lesson l -> l.id == 2L })
    }
}
