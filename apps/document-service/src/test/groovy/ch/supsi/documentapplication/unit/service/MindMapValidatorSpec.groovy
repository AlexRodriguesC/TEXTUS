package ch.supsi.documentapplication.unit.service

import ch.supsi.documentapplication.model.MindMap
import ch.supsi.documentapplication.service.MindMapValidator
import spock.lang.Specification
import spock.lang.Subject

class MindMapValidatorSpec extends Specification {

    @Subject
    MindMapValidator mindMapValidator = new MindMapValidator()

    MindMap mindMap = Mock()

    def "should return true if all chunkIds are present"() {
        given:
        List<Long> expectedChunks = [1L, 2L, 3L]
        mindMap.getAllChunkIds() >> expectedChunks

        when:
        boolean valid = mindMapValidator.validate(mindMap, expectedChunks)

        then:
        valid
    }

    def "should return false if chunks are missing"() {
        given:
        List<Long> expectedChunks = [1L, 2L, 3L]
        List<Long> actualChunks = [1L, 2L]
        mindMap.getAllChunkIds() >> actualChunks

        when:
        boolean valid = mindMapValidator.validate(mindMap, expectedChunks)

        then:
        !valid
    }

    def "should return false if #arg is null"() {
        when:
        boolean valid = mindMapValidator.validate(mindMap, expectedChunks)

        then:
        !valid

        where:
        arg              | mindMap | expectedChunks
        'mindmap'        | null    | []
        'expectedChunks' | Mock(MindMap)  | null
    }
}
