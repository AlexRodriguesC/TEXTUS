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
}
