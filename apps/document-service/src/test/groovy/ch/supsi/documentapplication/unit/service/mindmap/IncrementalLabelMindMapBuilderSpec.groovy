package ch.supsi.documentapplication.unit.service.mindmap

import ch.supsi.documentapplication.model.LabelMindMapNode
import ch.supsi.documentapplication.model.LabelTopicGroup
import ch.supsi.documentapplication.service.async.learningpipeline.Label
import ch.supsi.documentapplication.service.mindmap.IncrementalLabelMindMapBuilder
import ch.supsi.documentapplication.service.mindmap.MindMapExtractionSteps
import spock.lang.Specification

class IncrementalLabelMindMapBuilderSpec extends Specification {

    IncrementalLabelMindMapBuilder builder = new IncrementalLabelMindMapBuilder()

    def "should return single-node tree without step calls when only one label exists"() {
        given:
        def labels = [new Label(0L, "biology")]
        def steps = Mock(MindMapExtractionSteps)

        when:
        def result = builder.build(labels, [], steps)

        then:
        result.root.labelId == 0L
        result.root.children.isEmpty()
        0 * steps._
    }

    def "should build root with all remaining labels as direct children"() {
        given:
        def labels = [
                new Label(0L, "biology"),
                new Label(1L, "cell"),
                new Label(2L, "genetics")
        ]
        def steps = Mock(MindMapExtractionSteps)

        when:
        def result = builder.build(labels, [], steps)

        then:
        1 * steps.selectRoot(_, _) >> 0L
        1 * steps.selectChildren(_, _, _) >> [1L, 2L]
        result.allLabelIds == [0L, 1L, 2L] as Set
        result.root.children*.labelId == [1L, 2L]
    }

    def "should expand tree depth-first through BFS queue"() {
        given:
        def labels = [
                new Label(0L, "biology"),
                new Label(1L, "cell"),
                new Label(2L, "mitochondria")
        ]
        def steps = Mock(MindMapExtractionSteps)

        when:
        def result = builder.build(labels, [], steps)

        then:
        1 * steps.selectRoot(_, _) >> 0L
        1 * steps.selectChildren({ it.labelId == 0L }, _, _) >> [1L]
        1 * steps.selectChildren({ it.labelId == 1L }, _, _) >> [2L]
        result.root.labelId == 0L
        result.root.children*.labelId == [1L]
        result.root.children[0].children*.labelId == [2L]
    }

    def "should filter invalid duplicate and extra child ids from step responses"() {
        given:
        def labels = [
                new Label(0L, "biology"),
                new Label(1L, "cell"),
                new Label(2L, "genetics")
        ]
        def steps = Mock(MindMapExtractionSteps)

        when:
        def result = builder.build(labels, [], steps)

        then:
        1 * steps.selectRoot(_, _) >> 0L
        1 * steps.selectChildren(_, _, _) >> [1L, 1L, 99L, 2L]
        result.root.children*.labelId == [1L, 2L]
    }

    def "should return partial tree when queue is exhausted with remaining labels"() {
        given:
        def labels = [
                new Label(0L, "biology"),
                new Label(1L, "cell"),
                new Label(2L, "genetics")
        ]
        def steps = Mock(MindMapExtractionSteps)

        when:
        def result = builder.build(labels, [], steps)

        then:
        1 * steps.selectRoot(_, _) >> 0L
        1 * steps.selectChildren({ it.labelId == 0L }, _, _) >> [1L]
        1 * steps.selectChildren({ it.labelId == 1L }, _, _) >> []
        result.allLabelIds == [0L, 1L] as Set
    }

    def "should return empty tree when root selection is invalid"() {
        given:
        def labels = [new Label(0L, "biology"), new Label(1L, "cell")]
        def steps = Mock(MindMapExtractionSteps)

        when:
        def result = builder.build(labels, [], steps)

        then:
        1 * steps.selectRoot(_, _) >> 99L
        result.root == null
    }

    def "should pass label topic groups through to steps"() {
        given:
        def labels = [new Label(0L, "biology"), new Label(1L, "cell")]
        def groups = [new LabelTopicGroup(0L, [0, 1])]
        def steps = Mock(MindMapExtractionSteps)

        when:
        builder.build(labels, groups, steps)

        then:
        1 * steps.selectRoot(labels, groups) >> 0L
        1 * steps.selectChildren(_, [labels[1]], groups) >> [1L]
        noExceptionThrown()
    }
}
