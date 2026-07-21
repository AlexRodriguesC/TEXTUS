package ch.supsi.documentapplication.unit.service.validator

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException
import ch.supsi.documentapplication.model.LabelMindMap
import ch.supsi.documentapplication.model.LabelMindMapNode
import ch.supsi.documentapplication.service.async.learningpipeline.Label
import ch.supsi.documentapplication.service.validator.LabelMindMapValidator
import spock.lang.Specification

class LabelMindMapValidatorSpec extends Specification {

    LabelMindMapValidator validator = new LabelMindMapValidator()

    def "should throw when label mind map is null"() {
        when:
        validator.validate(null, [new Label(0L, "a")])

        then:
        thrown(AIOutputMappingFailedException)
    }

    def "should report missing label ids in feedback"() {
        given:
        def labels = [new Label(0L, "a"), new Label(1L, "b")]
        def labelMindMap = new LabelMindMap()
        labelMindMap.root = new LabelMindMapNode(0L)

        when:
        def feedback = validator.extractFeedback(labelMindMap, labels)

        then:
        !feedback.isValid()
        feedback.missingLabelIds() == [1L]
    }

    def "should report duplicate and extra label ids in feedback"() {
        given:
        def labels = [new Label(0L, "a"), new Label(1L, "b")]
        def labelMindMap = new LabelMindMap()
        labelMindMap.root = new LabelMindMapNode(0L, [
                new LabelMindMapNode(0L),
                new LabelMindMapNode(99L)
        ])

        when:
        def feedback = validator.extractFeedback(labelMindMap, labels)

        then:
        feedback.missingLabelIds() == [1L]
        feedback.duplicateLabelIds() == [0L]
        feedback.extraLabelIds() == [99L]
    }

    def "should succeed when all expected label ids are present"() {
        given:
        def labels = [new Label(0L, "a"), new Label(1L, "b")]
        def labelMindMap = new LabelMindMap()
        labelMindMap.root = new LabelMindMapNode(0L, [new LabelMindMapNode(1L)])

        when:
        validator.validate(labelMindMap, labels)

        then:
        noExceptionThrown()
    }
}
