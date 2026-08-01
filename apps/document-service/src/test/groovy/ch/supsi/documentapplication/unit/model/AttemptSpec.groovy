package ch.supsi.documentapplication.unit.model

import ch.supsi.documentapplication.controller.DTO.response.AttemptRequestDTO
import ch.supsi.documentapplication.model.Attempt
import ch.supsi.documentapplication.model.learn.ExerciseType
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.model.learn.exercise.Question
import spock.lang.Specification

class AttemptSpec extends Specification {

    def "should build question attempt from request"() {
        given:
        def exercise = new Question()
        exercise.setId(1L)
        exercise.setAnswer("4")
        def topic = new Topic(1L, "a.pdf", 1, 1, "title", "summary", [])
        def request = new AttemptRequestDTO(ExerciseType.QUESTION, "att-1", 1L, "4")

        when:
        def attempt = Attempt.from(request, exercise, topic)

        then:
        !attempt.isAnswerWrong()
    }

    def "should detect wrong question answer"() {
        given:
        def exercise = new Question()
        exercise.setId(1L)
        exercise.setAnswer("4")
        def topic = new Topic(1L, "a.pdf", 1, 1, "title", "summary", [])
        def request = new AttemptRequestDTO(ExerciseType.QUESTION, "att-1", 1L, "3")

        when:
        def attempt = Attempt.from(request, exercise, topic)

        then:
        attempt.isAnswerWrong()
    }

    def "should detect null question response as wrong when skipped"() {
        given:
        def exercise = new Question()
        exercise.setId(1L)
        exercise.setAnswer("4")
        def topic = new Topic(1L, "a.pdf", 1, 1, "title", "summary", [])
        def request = new AttemptRequestDTO(ExerciseType.QUESTION, "att-skip", 1L, null)

        when:
        def attempt = Attempt.from(request, exercise, topic)

        then:
        attempt.isAnswerWrong()
    }

    def "should detect null list response as wrong when skipped"() {
        given:
        def exercise = new ch.supsi.documentapplication.model.learn.exercise.List()
        exercise.setId(2L)
        exercise.setAnswer(["a", "b"])
        def topic = new Topic(1L, "a.pdf", 1, 1, "title", "summary", [])
        def request = new AttemptRequestDTO(ExerciseType.LIST, "att-skip", 2L, null)

        when:
        def attempt = Attempt.from(request, exercise, topic)

        then:
        attempt.isAnswerWrong()
    }

    def "should build list attempt from request"() {
        given:
        def exercise = new ch.supsi.documentapplication.model.learn.exercise.List()
        exercise.setId(2L)
        exercise.setAnswer(["a", "b"])
        def topic = new Topic(1L, "a.pdf", 1, 1, "title", "summary", [])
        def request = new AttemptRequestDTO(ExerciseType.LIST, "att-2", 2L, ["a", "b"])

        when:
        def attempt = Attempt.from(request, exercise, topic)

        then:
        !attempt.isAnswerWrong()
        attempt.exerciseType == ExerciseType.LIST
    }
}
