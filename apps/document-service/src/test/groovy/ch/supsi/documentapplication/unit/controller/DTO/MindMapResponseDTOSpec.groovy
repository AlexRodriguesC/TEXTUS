package ch.supsi.documentapplication.unit.controller.DTO

import ch.supsi.documentapplication.controller.DTO.response.MindMapResponseDTO
import ch.supsi.documentapplication.model.MindMap
import ch.supsi.documentapplication.model.MindMapNode
import spock.lang.Specification

class MindMapResponseDTOSpec extends Specification {

    def "should flatten a single root page node"() {
        given:
        def mindMap = new MindMap()
        mindMap.root = MindMapNode.page(10L)

        when:
        def dto = MindMapResponseDTO.from(mindMap)

        then:
        dto.nodes.size() == 1
        dto.nodes[0].nodeType == MindMapResponseDTO.NodeType.PAGE
        dto.nodes[0].entityId == 10L
        dto.nodes[0].parentNodeType == null
        dto.nodes[0].parentEntityId == null
    }

    def "should flatten mixed page and topic nodes with parent references"() {
        given:
        def root = MindMapNode.page(10L)
        def topic = MindMapNode.topic(2L)
        def childPage = MindMapNode.page(20L)
        root.children = [topic, childPage]

        def mindMap = new MindMap()
        mindMap.root = root

        when:
        def dto = MindMapResponseDTO.from(mindMap)

        then:
        dto.nodes.size() == 3
        dto.nodes[0].nodeType == MindMapResponseDTO.NodeType.PAGE
        dto.nodes[0].entityId == 10L
        dto.nodes[1].nodeType == MindMapResponseDTO.NodeType.TOPIC
        dto.nodes[1].entityId == 2L
        dto.nodes[1].parentNodeType == MindMapResponseDTO.NodeType.PAGE
        dto.nodes[1].parentEntityId == 10L
        dto.nodes[2].nodeType == MindMapResponseDTO.NodeType.PAGE
        dto.nodes[2].entityId == 20L
        dto.nodes[2].parentNodeType == MindMapResponseDTO.NodeType.PAGE
        dto.nodes[2].parentEntityId == 10L
    }

    def "should return empty nodes when mind map has no root"() {
        given:
        def mindMap = new MindMap()

        when:
        def dto = MindMapResponseDTO.from(mindMap)

        then:
        dto.nodes.isEmpty()
    }
}
