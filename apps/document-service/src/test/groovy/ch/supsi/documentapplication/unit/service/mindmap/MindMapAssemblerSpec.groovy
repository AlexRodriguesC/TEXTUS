package ch.supsi.documentapplication.unit.service.mindmap

import ch.supsi.documentapplication.model.LabelMindMap
import ch.supsi.documentapplication.model.LabelMindMapNode
import ch.supsi.documentapplication.model.MindMap
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.async.learningpipeline.Label
import ch.supsi.documentapplication.service.mindmap.MindMapAssembler
import spock.lang.Specification

class MindMapAssemblerSpec extends Specification {

    MindMapAssembler assembler = new MindMapAssembler()

    static Topic topic(Long id, List<String> labels) {
        new Topic(id, "doc.pdf", 1, "Title $id", "Summary $id", labels)
    }

    def "should assemble a topic tree from a label tree"() {
        given:
        def topics = [
                topic(1L, ["biology"]),
                topic(2L, ["biology", "cell"]),
                topic(3L, ["cell"])
        ]
        def labels = [
                new Label(0L, "biology"),
                new Label(1L, "cell")
        ]
        def labelMindMap = new LabelMindMap()
        labelMindMap.root = new LabelMindMapNode(0L, [new LabelMindMapNode(1L)])

        when:
        MindMap result = assembler.assemble(labelMindMap, topics, labels)

        then:
        result.root.topicId == 1L
        result.allTopicIds == [1L, 2L, 3L] as Set
    }

    def "should throw when topic has no labels"() {
        given:
        def topics = [new Topic(1L, "doc.pdf", 1, "T", "S", [])]
        def labels = [new Label(0L, "biology")]
        def labelMindMap = new LabelMindMap()
        labelMindMap.root = new LabelMindMapNode(0L)

        when:
        assembler.assemble(labelMindMap, topics, labels)

        then:
        thrown(IllegalStateException)
    }

    def "should build labels and topic groups from topics"() {
        given:
        def topics = [
                topic(1L, ["a", "b"]),
                topic(2L, ["b"])
        ]

        when:
        def labels = MindMapAssembler.buildLabels(topics)
        def groups = MindMapAssembler.buildLabelTopicGroups(topics, labels)

        then:
        labels*.name() == ["a", "b"]
        groups.find { it.labelId() == 0L }.topicIndices() == [0]
        groups.find { it.labelId() == 1L }.topicIndices() == [0, 1]
    }
}
