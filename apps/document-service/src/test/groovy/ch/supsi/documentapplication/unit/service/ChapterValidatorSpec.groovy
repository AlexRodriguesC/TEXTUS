package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException
import ch.supsi.documentapplication.model.learn.Chapter
import ch.supsi.documentapplication.service.async.learningpipeline.Label
import ch.supsi.documentapplication.service.validator.ChapterValidator
import spock.lang.Specification
import spock.lang.Subject

class ChapterValidatorSpec extends Specification {

    @Subject
    ChapterValidator validator = new ChapterValidator()

    def "should throw when chapters is null"() {
        when:
        validator.validate(null, [new Label(1L, "a")])

        then:
        thrown(AIOutputMappingFailedException)
    }

    def "should throw when labels is null"() {
        given:
        def chapter = new Chapter(0, "Intro", "desc", [1L])

        when:
        validator.validate([chapter], null)

        then:
        thrown(AIOutputMappingFailedException)
    }

    def "should throw when label id is missing from chapters"() {
        given:
        def chapter = new Chapter(0, "Intro", "desc", [1L, 3L])
        def labels = [new Label(1L, "a"), new Label(2L, "b")]

        when:
        validator.validate([chapter], labels)

        then:
        thrown(AIOutputMappingFailedException)
    }

    def "should succeed when chapters contain all expected label ids"() {
        given:
        def chapter = new Chapter(0, "Intro", "desc", [1L, 2L])
        def labels = [new Label(1L, "a"), new Label(2L, "b")]

        when:
        validator.validate([chapter], labels)

        then:
        noExceptionThrown()
    }

    def "should succeed when chapters contain duplicate label ids but all expected ids are present"() {
        given:
        def chapters = [
                new Chapter(0, "Intro", "desc", [1L, 2L]),
                new Chapter(1, "Advanced", "desc", [2L, 3L])
        ]
        def labels = [new Label(1L, "a"), new Label(2L, "b"), new Label(3L, "c")]

        when:
        validator.validate(chapters, labels)

        then:
        noExceptionThrown()
    }

    def "should report duplicate label ids in feedback without marking invalid"() {
        given:
        def chapters = [
                new Chapter(0, "Intro", "desc", [1L, 2L]),
                new Chapter(1, "Advanced", "desc", [2L, 3L])
        ]
        def labels = [new Label(1L, "a"), new Label(2L, "b"), new Label(3L, "c")]

        when:
        def feedback = validator.extractFeedback(chapters, labels)

        then:
        feedback.duplicateLabelIds() == [2L]
        feedback.isValid()
    }

    def "should succeed when chapters contain extra label ids but all expected ids are present"() {
        given:
        def chapter = new Chapter(0, "Intro", "desc", [1L, 2L, 99L])
        def labels = [new Label(1L, "a"), new Label(2L, "b")]

        when:
        validator.validate([chapter], labels)

        then:
        noExceptionThrown()
    }

    def "should report extra label ids in feedback without marking invalid"() {
        given:
        def chapter = new Chapter(0, "Intro", "desc", [1L, 2L, 99L])
        def labels = [new Label(1L, "a"), new Label(2L, "b")]

        when:
        def feedback = validator.extractFeedback([chapter], labels)

        then:
        feedback.extraLabelIds() == [99L]
        feedback.isValid()
    }
}
