package ch.supsi.documentapplication.unit.model

import ch.supsi.documentapplication.model.learn.exercise.Group
import ch.supsi.documentapplication.model.learn.exercise.LeftRight
import ch.supsi.documentapplication.model.learn.exercise.PairExercise
import ch.supsi.documentapplication.model.learn.exercise.Question
import spock.lang.Specification

class ExerciseSpec extends Specification {

    def "should validate question answer correctly"() {
        given:
        def question = new Question()
        question.setAnswer("4")
        question.setDistractor(["3", "5"])

        expect:
        question.isAnswerCorrect("4")
        !question.isAnswerCorrect("3")
        question.guessProbability == 1.0d / 3.0d
    }

    def "should validate list answer correctly"() {
        given:
        def list = new ch.supsi.documentapplication.model.learn.exercise.List()
        list.setAnswer(["a", "b", "c"])

        expect:
        list.isAnswerCorrect(["a", "b", "c"])
        !list.isAnswerCorrect(["a", "c", "b"])
    }

    def "should validate group answer correctly"() {
        given:
        def group = new Group()
        group.setAnswer([fruit: ["apple"], vegetable: ["carrot"]])

        expect:
        group.isAnswerCorrect([fruit: ["apple"], vegetable: ["carrot"]])
        group.isAnswerCorrect([fruit: ["apple"], vegetable: ["carrot"]])
        !group.isAnswerCorrect([fruit: ["banana"], vegetable: ["carrot"]])
        group.guessProbability == 0.05d
    }

    def "should validate pair answer correctly"() {
        given:
        def pair = new PairExercise()
        pair.setAnswer([new LeftRight("cat", "meow"), new LeftRight("dog", "bark")])

        expect:
        pair.isAnswerCorrect([new LeftRight("cat", "meow"), new LeftRight("dog", "bark")])
        !pair.isAnswerCorrect([new LeftRight("cat", "bark"), new LeftRight("dog", "meow")])
    }
}
