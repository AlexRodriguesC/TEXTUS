package ch.supsi.documentapplication.unit.model

import ch.supsi.documentapplication.model.MindMap
import ch.supsi.documentapplication.model.MindMapNode
import ch.supsi.documentapplication.model.MindMapNodeType
import spock.lang.Specification

class MindMapSpec extends Specification {

    def "should collect all topic and page ids from tree"() {
        given:
        def root = MindMapNode.page(10L)
        def topic = MindMapNode.topic(2L)
        def childPage = MindMapNode.page(20L)
        root.children = [topic, childPage]

        def mindMap = new MindMap()
        mindMap.root = root

        expect:
        mindMap.allTopicIds == [2L] as Set
        mindMap.allPageIds == [10L, 20L] as Set
    }

    def "should report empty when root is null"() {
        given:
        def mindMap = new MindMap()

        expect:
        mindMap.isEmpty()
    }
}
