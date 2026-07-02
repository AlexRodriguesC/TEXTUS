package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException
import ch.supsi.documentapplication.model.MindMap
import ch.supsi.documentapplication.service.MindMapValidator
import spock.lang.Specification
import spock.lang.Subject

class MindMapValidatorSpec extends Specification {

    @Subject
    MindMapValidator validator = new MindMapValidator()

    MindMap mindMap = Mock()

    def "should return true when all expected chunk ids are present"() {
        given: "a mind map containing all expected chunk ids"
        mindMap.getAllTopicIds() >> [1L, 2L, 3L]
        def expectedChunkIds = [1L, 2L]

        when: "validating the mind map"
        def result = validator.validate(mindMap, expectedChunkIds)

        then: "the validation succeeds"
        result
    }

    def "should throw when mindMap is null"() {
        when: "validating a null mind map"
        validator.validate(null, [1L])

        then: "an AIOutputMappingFailedException is thrown"
        thrown(AIOutputMappingFailedException)
    }

    def "should throw when expectedChunkIds is null"() {
        when: "validating with null expected chunk ids"
        validator.validate(mindMap, null)

        then: "an AIOutputMappingFailedException is thrown"
        thrown(AIOutputMappingFailedException)
    }

    def "should throw when a chunk id is missing from the mind map"() {
        given: "a mind map missing one expected chunk id"
        mindMap.getAllTopicIds() >> [1L, 3L]
        def expectedChunkIds = [1L, 2L]

        when: "validating the mind map"
        validator.validate(mindMap, expectedChunkIds)

        then: "an AIOutputMappingFailedException is thrown"
        thrown(AIOutputMappingFailedException)
    }

    def "should return true when expectedChunkIds is empty"() {
        given: "a mind map and no expected chunk ids"
        mindMap.getAllTopicIds() >> [1L, 2L]

        when: "validating with an empty expected list"
        def result = validator.validate(mindMap, [])

        then: "the validation succeeds"
        result
    }

    def "should throw when mind map has no topic ids but ids are expected"() {
        given: "a mind map with no topic ids"
        mindMap.getAllTopicIds() >> []
        def expectedChunkIds = [1L]

        when: "validating the mind map"
        validator.validate(mindMap, expectedChunkIds)

        then: "an AIOutputMappingFailedException is thrown"
        thrown(AIOutputMappingFailedException)
    }
}
