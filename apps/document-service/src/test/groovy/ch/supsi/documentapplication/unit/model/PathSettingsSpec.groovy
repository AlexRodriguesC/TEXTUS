package ch.supsi.documentapplication.unit.model

import ch.supsi.documentapplication.controller.DTO.request.PatchSettingsDTO
import ch.supsi.documentapplication.exception.InvalidInputException
import ch.supsi.documentapplication.model.PathSettings
import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

class PathSettingsSpec extends Specification {

    def "should map from PatchSettingsDTO"() {
        given:
        def dto = new ObjectMapper().readValue(
                '{"questionPercentage":25,"listPercentage":25,"groupPercentage":25,"pairPercentage":25}',
                PatchSettingsDTO)

        when:
        def settings = PathSettings.from(dto)

        then:
        settings.questionPercentage == 25
        settings.listPercentage == 25
        settings.groupPercentage == 25
        settings.pairPercentage == 25
    }

    def "should validate when percentages sum to 100"() {
        given:
        def settings = new PathSettings(40, 30, 20, 10)

        when:
        settings.validate()

        then:
        noExceptionThrown()
    }

    def "should throw when percentages do not sum to 100"() {
        given:
        def settings = new PathSettings(40, 30, 20, 5)

        when:
        settings.validate()

        then:
        thrown(InvalidInputException)
    }
}
