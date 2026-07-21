package ch.supsi.documentapplication.unit.model

import ch.supsi.documentapplication.model.MindMap
import ch.supsi.documentapplication.model.MindMapNode
import spock.lang.Specification

class MindMapSpec extends Specification {

    def "should report empty when root is null"() {
        given:
        def mindMap = new MindMap()

        expect:
        mindMap.isEmpty()
        mindMap.allTopicIds.isEmpty()
    }

    def "should collect all topic ids from tree"() {
        given:
        def child = new MindMapNode(2L)
        def root = new MindMapNode(1L)
        root.children = [child]
        def mindMap = new MindMap()
        mindMap.root = root

        expect:
        !mindMap.isEmpty()
        mindMap.allTopicIds == [1L, 2L] as Set
    }
}
