package ch.supsi.documentapplication.unit.model

import ch.supsi.documentapplication.model.MindMapNode
import ch.supsi.documentapplication.model.MindMapNodeDeserializer
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

    def "should deserialize topicId field"() {
        when:
        def node = objectMapper.readValue('{"topicId": 42, "children": []}', MindMapNode.class)

        then:
        node.topicId == 42L
        node.children.isEmpty()
    }

    def "should deserialize chunkId field as fallback"() {
        when:
        def node = objectMapper.readValue('{"chunkId": 7, "children": []}', MindMapNode.class)

        then:
        node.topicId == 7L
    }
}
