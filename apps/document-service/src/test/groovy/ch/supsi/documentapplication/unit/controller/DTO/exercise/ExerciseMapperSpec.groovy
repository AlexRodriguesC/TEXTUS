package ch.supsi.documentapplication.unit.controller.DTO.exercise

import ch.supsi.documentapplication.controller.DTO.response.exercise.ExerciseMapper
import ch.supsi.documentapplication.controller.DTO.response.exercise.GroupResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.exercise.ListResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.exercise.PairResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.exercise.QuestionResponseDTO
import ch.supsi.documentapplication.model.learn.ExerciseType
import ch.supsi.documentapplication.model.learn.exercise.Group
import ch.supsi.documentapplication.model.learn.exercise.LeftRight
import ch.supsi.documentapplication.model.learn.exercise.PairExercise
import ch.supsi.documentapplication.model.learn.exercise.Question
import spock.lang.Specification
import spock.lang.Subject

class ExerciseMapperSpec extends Specification {

    @Subject
    ExerciseMapper mapper = new ExerciseMapper()

    def "should map question exercise to DTO"() {
        given:
        def question = new Question()
        question.setId(1L)
        question.setTopicId(2L)
        question.setRequest("What is 2+2?")
        question.setAnswer("4")
        question.setDistractor(["3", "5"])

        when:
        def dto = mapper.toDto(question)

        then:
        dto instanceof QuestionResponseDTO
        dto.id() == 1L
        dto.type() == ExerciseType.QUESTION
    }

    def "should map list exercise to DTO"() {
        given:
        def list = new ch.supsi.documentapplication.model.learn.exercise.List()
        list.setId(1L)
        list.setTopicId(2L)
        list.setRequest("Order")
        list.setAnswer(["a", "b"])

        when:
        def dto = mapper.toDto(list)

        then:
        dto instanceof ListResponseDTO
        dto.type() == ExerciseType.LIST
    }

    def "should map group exercise to DTO"() {
        given:
        def group = new Group()
        group.setId(1L)
        group.setTopicId(2L)
        group.setRequest("Group")
        group.setAnswer([a: ["1"]])

        when:
        def dto = mapper.toDto(group)

        then:
        dto instanceof GroupResponseDTO
        dto.type() == ExerciseType.GROUP
    }

    def "should map pair exercise to DTO"() {
        given:
        def pair = new PairExercise()
        pair.setId(1L)
        pair.setTopicId(2L)
        pair.setRequest("Match")
        pair.setAnswer([new LeftRight("a", "b")])

        when:
        def dto = mapper.toDto(pair)

        then:
        dto instanceof PairResponseDTO
        dto.type() == ExerciseType.PAIR
    }
}
