package ch.supsi.documentapplication.unit.service.mindmap

import ch.supsi.documentapplication.model.Page
import ch.supsi.documentapplication.model.PageMindMap
import ch.supsi.documentapplication.model.PageMindMapNode
import ch.supsi.documentapplication.service.mindmap.PageMindMapTreeUtils
import spock.lang.Specification

class PageMindMapTreeUtilsSpec extends Specification {

    def "should flatten page tree to skeleton with node indices"() {
        given:
        def root = new PageMindMapNode(10L, [new PageMindMapNode(20L)])
        def pageTitles = [(10L): "Intro", (20L): "Methods"]

        when:
        def skeletons = PageMindMapTreeUtils.flattenToSkeleton(root, pageTitles)

        then:
        skeletons.size() == 2
        skeletons[0].nodeIndex() == 0
        skeletons[0].pageTitle() == "Intro"
        skeletons[0].parentNodeIndex() == null
        skeletons[1].nodeIndex() == 1
        skeletons[1].pageTitle() == "Methods"
        skeletons[1].parentNodeIndex() == 0
    }

    def "should merge missing pages into existing tree"() {
        given:
        def pageMindMap = new PageMindMap()
        pageMindMap.root = new PageMindMapNode(10L, [new PageMindMapNode(20L)])

        when:
        PageMindMapTreeUtils.mergeMissingPages(pageMindMap, [(0): [30L, 40L]])

        then:
        pageMindMap.root.children*.pageId == [20L, 30L, 40L]
    }

    def "should sanitize duplicate and extra page ids"() {
        given:
        def pages = [new Page(10L, 1, "A", "S"), new Page(20L, 1, "B", "S")]
        def pageMindMap = new PageMindMap()
        pageMindMap.root = new PageMindMapNode(10L, [
                new PageMindMapNode(10L),
                new PageMindMapNode(99L),
                new PageMindMapNode(20L)
        ])

        when:
        def sanitized = PageMindMapTreeUtils.sanitize(pageMindMap, pages)

        then:
        sanitized.allPageIds == [10L, 20L] as Set
    }
}
