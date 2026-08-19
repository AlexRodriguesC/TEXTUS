package ch.supsi.documentapplication.unit.model

import ch.supsi.documentapplication.model.MindMapNode
import ch.supsi.documentapplication.model.MindMapNodeDeserializer
import ch.supsi.documentapplication.model.MindMapNodeType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import spock.lang.Specification

class MindMapNodeDeserializerSpec extends Specification {

    ObjectMapper objectMapper

    def setup() {
        objectMapper = new ObjectMapper()
        SimpleModule module = new SimpleModule()
        module.addDeserializer(MindMapNode.class, new MindMapNodeDeserializer())
        objectMapper.registerModule(module)
    }

    def "should deserialize typed page node"() {
        when:
        def node = objectMapper.readValue(
                '{"type":"PAGE","pageId":10,"children":[]}',
                MindMapNode.class)

        then:
        node.type == MindMapNodeType.PAGE
        node.pageId == 10L
        node.topicId == null
    }

    def "should deserialize typed topic node"() {
        when:
        def node = objectMapper.readValue(
                '{"type":"TOPIC","topicId":7,"children":[]}',
                MindMapNode.class)

        then:
        node.type == MindMapNodeType.TOPIC
        node.topicId == 7L
        node.pageId == null
    }
}
