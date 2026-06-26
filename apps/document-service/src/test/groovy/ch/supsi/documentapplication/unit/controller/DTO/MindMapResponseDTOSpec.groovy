package ch.supsi.documentapplication.unit.controller.DTO

import ch.supsi.documentapplication.controller.DTO.response.MindMapResponseDTO
import ch.supsi.documentapplication.model.MindMap
import ch.supsi.documentapplication.model.MindMapNode
import spock.lang.Specification

class MindMapResponseDTOSpec extends Specification {

    def "should return empty nodes list when mind map is null"() {
        when:
        def dto = MindMapResponseDTO.from(null)

        then:
        dto.nodes != null
        dto.nodes.isEmpty()
    }

    def "should return empty nodes list when root is null"() {
        given:
        def mindMap = new MindMap()
        mindMap.setRoot(null)

        when:
        def dto = MindMapResponseDTO.from(mindMap)

        then:
        dto.nodes != null
        dto.nodes.isEmpty()
    }

    def "should flatten a single root node with null parent"() {
        given:
        def root = new MindMapNode(10L)
        def mindMap = new MindMap()
        mindMap.setRoot(root)

        when:
        def dto = MindMapResponseDTO.from(mindMap)

        then:
        dto.nodes.size() == 1
        dto.nodes[0].chunkId == 10L
        dto.nodes[0].parentChunkId == null
    }

    def "should flatten a tree in depth first order with parent ids"() {
        given:
        def root = new MindMapNode(1L)
        def childA = new MindMapNode(2L)
        def childB = new MindMapNode(3L)
        def grandChild = new MindMapNode(4L)

        childA.getChildren().add(grandChild)
        root.getChildren().add(childA)
        root.getChildren().add(childB)

        def mindMap = new MindMap()
        mindMap.setRoot(root)

        when:
        def dto = MindMapResponseDTO.from(mindMap)

        then:
        dto.nodes.size() == 4
        dto.nodes[0].chunkId == 1L
        dto.nodes[0].parentChunkId == null

        dto.nodes[1].chunkId == 2L
        dto.nodes[1].parentChunkId == 1L

        dto.nodes[2].chunkId == 4L
        dto.nodes[2].parentChunkId == 2L

        dto.nodes[3].chunkId == 3L
        dto.nodes[3].parentChunkId == 1L
    }

    def "should include a node even when children are null"() {
        given:
        def root = new MindMapNode(1L)
        root.setChildren(null)

        def mindMap = new MindMap()
        mindMap.setRoot(root)

        when:
        def dto = MindMapResponseDTO.from(mindMap)

        then:
        dto.nodes.size() == 1
        dto.nodes[0].chunkId == 1L
        dto.nodes[0].parentChunkId == null
    }
}
