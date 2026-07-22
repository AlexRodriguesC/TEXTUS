package ch.supsi.documentapplication.unit.service.mindmap

import ch.supsi.documentapplication.model.Page
import ch.supsi.documentapplication.model.PageMindMapNode
import ch.supsi.documentapplication.model.PageTopicGroup
import ch.supsi.documentapplication.service.mindmap.IncrementalPageMindMapBuilder
import ch.supsi.documentapplication.service.mindmap.MindMapExtractionSteps
import spock.lang.Specification

class IncrementalPageMindMapBuilderSpec extends Specification {

    IncrementalPageMindMapBuilder builder = new IncrementalPageMindMapBuilder()

    static Page page(Long id) {
        new Page(id, "Page $id", "Summary $id")
    }

    def "should return single-node tree without step calls when only one page exists"() {
        given:
        def pages = [page(10L)]
        def steps = Mock(MindMapExtractionSteps)

        when:
        def result = builder.build(pages, [], steps)

        then:
        result.root.pageId == 10L
        result.root.children.isEmpty()
        0 * steps._
    }

    def "should build root with all remaining pages as direct children"() {
        given:
        def pages = [page(10L), page(20L), page(30L)]
        def steps = Mock(MindMapExtractionSteps)

        when:
        def result = builder.build(pages, [], steps)

        then:
        1 * steps.selectRoot(_, _) >> 10L
        1 * steps.selectChildren(_, _, _) >> [20L, 30L]
        result.allPageIds == [10L, 20L, 30L] as Set
        result.root.children*.pageId == [20L, 30L]
    }

    def "should expand tree depth-first through BFS queue"() {
        given:
        def pages = [page(10L), page(20L), page(30L)]
        def steps = Mock(MindMapExtractionSteps)

        when:
        def result = builder.build(pages, [], steps)

        then:
        1 * steps.selectRoot(_, _) >> 10L
        1 * steps.selectChildren({ it.pageId == 10L }, _, _) >> [20L]
        1 * steps.selectChildren({ it.pageId == 20L }, _, _) >> [30L]
        result.root.pageId == 10L
        result.root.children*.pageId == [20L]
        result.root.children[0].children*.pageId == [30L]
    }

    def "should pass page topic groups through to steps"() {
        given:
        def pages = [page(10L), page(20L)]
        def groups = [new PageTopicGroup(10L, [0, 1])]
        def steps = Mock(MindMapExtractionSteps)

        when:
        builder.build(pages, groups, steps)

        then:
        1 * steps.selectRoot(pages, groups) >> 10L
        1 * steps.selectChildren(_, [pages[1]], groups) >> [20L]
        noExceptionThrown()
    }
}
