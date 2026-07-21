package ch.supsi.documentapplication.unit.model

import ch.supsi.documentapplication.model.LessonStatus
import ch.supsi.documentapplication.model.learn.Lesson
import ch.supsi.documentapplication.model.learn.LessonType
import ch.supsi.documentapplication.model.learn.exercise.Question
import spock.lang.Specification

class LessonSpec extends Specification {

    def "should allow start when prerequisite is null"() {
        given:
        def lesson = new Lesson(1L, 1L, 0, LessonType.LESSON, [10L], 0L, 5, 0.10d)

        expect:
        lesson.canBeStarted(null)
    }

    def "should allow start when prerequisite is mastered"() {
        given:
        def prerequisite = new Lesson(1L, 1L, 0, LessonType.LESSON, [10L], 0L, 5, 0.96d)
        def lesson = new Lesson(2L, 1L, 1, LessonType.LESSON, [11L], 0L, 5, 0.10d)

        expect:
        lesson.canBeStarted(prerequisite)
    }

    def "should not allow start when prerequisite is not mastered"() {
        given:
        def prerequisite = new Lesson(1L, 1L, 0, LessonType.LESSON, [10L], 0L, 5, 0.50d)
        def lesson = new Lesson(2L, 1L, 1, LessonType.LESSON, [11L], 0L, 5, 0.10d)

        expect:
        !lesson.canBeStarted(prerequisite)
    }

    def "should detect checkpoint lesson type"() {
        given:
        def checkpoint = new Lesson(1L, 1L, 1, LessonType.CHECKPOINT, [], 0L, 5, 0.10d)

        expect:
        checkpoint.isCheckpoint()
    }

    def "should consume attempt and advance topic cyclically"() {
        given:
        def lesson = new Lesson(1L, 1L, 0, LessonType.LESSON, [10L, 20L, 30L], 0L, 5, 0.10d)

        when:
        lesson.consumeAttempt()
        lesson.advanceCurrentTopic()

        then:
        lesson.attemptsLeft == 4
        lesson.currentTopicId == 20L

        when:
        lesson.advanceCurrentTopic()
        lesson.advanceCurrentTopic()

        then:
        lesson.currentTopicId == 10L
    }

    def "should report mastered status when mastery reaches threshold"() {
        given:
        def lesson = new Lesson(1L, 1L, 0, LessonType.LESSON, [10L], 0L, 5, 0.96d)

        expect:
        lesson.isMastered()
        lesson.status == LessonStatus.MASTERED
    }

    def "should track exercise in progress"() {
        given:
        def lesson = new Lesson(1L, 1L, 0, LessonType.LESSON, [10L], 0L, 5, 0.10d)
        def exercise = new Question()
        exercise.setCompleted(false)

        when:
        lesson.addExercise(exercise)

        then:
        lesson.hasExerciseInProgress()
        lesson.exerciseInProgress == exercise
    }

    def "should reset mastery and attempts"() {
        given:
        def lesson = new Lesson(1L, 1L, 0, LessonType.LESSON, [10L], 0L, 1, 0.80d)

        when:
        lesson.reset()

        then:
        lesson.mastery == Lesson.INITIAL_MASTERY
        lesson.attemptsLeft == lesson.maxAttempts
    }
}
