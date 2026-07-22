package ch.supsi.documentapplication.unit.service.mindmap

import ch.supsi.documentapplication.model.MindMapNodeType
import ch.supsi.documentapplication.model.Page
import ch.supsi.documentapplication.model.PageMindMap
import ch.supsi.documentapplication.model.PageMindMapNode
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.mindmap.MindMapAssembler
import spock.lang.Specification

class MindMapAssemblerSpec extends Specification {

    MindMapAssembler assembler = new MindMapAssembler()

    static Topic topic(Long id, Long pageId) {
        new Topic(id, "doc.pdf", pageId, "Title $id", "Summary $id", ["label"])
    }

    def "should assemble a mixed page and topic tree from a page tree"() {
        given:
        def pages = [
                new Page(10L, "Intro", "Intro summary"),
                new Page(20L, "Methods", "Methods summary")
        ]
        def topics = [
                topic(1L, 10L),
                topic(2L, 10L),
                topic(3L, 20L)
        ]
        def pageMindMap = new PageMindMap()
        pageMindMap.root = new PageMindMapNode(10L, [new PageMindMapNode(20L)])

        when:
        def result = assembler.assemble(pageMindMap, pages, topics)

        then:
        result.root.type == MindMapNodeType.PAGE
        result.root.pageId == 10L
        result.root.children*.type == [MindMapNodeType.TOPIC, MindMapNodeType.TOPIC, MindMapNodeType.PAGE]
        result.allTopicIds == [1L, 2L, 3L] as Set
        result.allPageIds == [10L, 20L] as Set
    }

    def "should build page topic groups from pages and topics"() {
        given:
        def pages = [new Page(10L, "A", "S"), new Page(20L, "B", "S")]
        def topics = [topic(1L, 10L), topic(2L, 10L), topic(3L, 20L)]

        when:
        def groups = MindMapAssembler.buildPageTopicGroups(pages, topics)

        then:
        groups.find { it.pageId() == 10L }.topicIndices() == [0, 1]
        groups.find { it.pageId() == 20L }.topicIndices() == [0]
    }
}
