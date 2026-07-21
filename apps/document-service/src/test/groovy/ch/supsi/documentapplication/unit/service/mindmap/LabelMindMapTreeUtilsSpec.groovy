package ch.supsi.documentapplication.unit.service.mindmap

import ch.supsi.documentapplication.model.LabelMindMap
import ch.supsi.documentapplication.model.LabelMindMapNode
import ch.supsi.documentapplication.service.async.learningpipeline.Label
import ch.supsi.documentapplication.service.mindmap.LabelMindMapTreeUtils
import spock.lang.Specification

class LabelMindMapTreeUtilsSpec extends Specification {

    def "should flatten tree to indexed skeletons"() {
        given:
        def root = new LabelMindMapNode(0L, [new LabelMindMapNode(1L)])
        def labelNames = [(0L): "biology", (1L): "cell"]

        when:
        def skeletons = LabelMindMapTreeUtils.flattenToSkeleton(root, labelNames)

        then:
        skeletons.size() == 2
        skeletons[0].nodeIndex() == 0
        skeletons[0].parentNodeIndex() == null
        skeletons[1].nodeIndex() == 1
        skeletons[1].parentNodeIndex() == 0
    }

    def "should merge missing labels into assigned parent nodes"() {
        given:
        def labelMindMap = new LabelMindMap()
        labelMindMap.root = new LabelMindMapNode(0L, [new LabelMindMapNode(1L)])

        when:
        LabelMindMapTreeUtils.mergeMissingLabels(labelMindMap, [(0): [2L, 3L]])

        then:
        labelMindMap.root.children.size() == 3
        labelMindMap.root.children*.labelId.containsAll([1L, 2L, 3L])
    }

    def "should attach children to parent node"() {
        given:
        def parent = new LabelMindMapNode(0L)

        when:
        LabelMindMapTreeUtils.attachChildren(parent, [1L, 2L])

        then:
        parent.children*.labelId == [1L, 2L]
    }

    def "should sanitize duplicates and extras"() {
        given:
        def labelMindMap = new LabelMindMap()
        labelMindMap.root = new LabelMindMapNode(0L, [
                new LabelMindMapNode(0L),
                new LabelMindMapNode(99L),
                new LabelMindMapNode(1L)
        ])
        def labels = [new Label(0L, "a"), new Label(1L, "b")]

        when:
        def sanitized = LabelMindMapTreeUtils.sanitize(labelMindMap, labels)

        then:
        sanitized.allLabelIds == [0L, 1L] as Set
    }
}
