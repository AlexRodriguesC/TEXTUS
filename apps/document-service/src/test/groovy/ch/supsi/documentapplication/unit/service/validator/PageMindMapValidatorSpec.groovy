package ch.supsi.documentapplication.unit.service.validator

import ch.supsi.documentapplication.exception.AIOutputMappingFailedException
import ch.supsi.documentapplication.model.Page
import ch.supsi.documentapplication.model.PageMindMap
import ch.supsi.documentapplication.model.PageMindMapNode
import ch.supsi.documentapplication.service.validator.PageMindMapValidator
import spock.lang.Specification

class PageMindMapValidatorSpec extends Specification {

    PageMindMapValidator validator = new PageMindMapValidator()

    static Page page(Long id) {
        new Page(id, "Page $id", "Summary $id")
    }

    def "should validate when all pages are present exactly once"() {
        given:
        def pages = [page(10L), page(20L)]
        def pageMindMap = new PageMindMap()
        pageMindMap.root = new PageMindMapNode(10L, [new PageMindMapNode(20L)])

        when:
        validator.validate(pageMindMap, pages)

        then:
        noExceptionThrown()
    }

    def "should throw when pages are missing"() {
        given:
        def pages = [page(10L), page(20L)]
        def pageMindMap = new PageMindMap()
        pageMindMap.root = new PageMindMapNode(10L)

        when:
        validator.validate(pageMindMap, pages)

        then:
        thrown(AIOutputMappingFailedException)
    }

    def "should report duplicate and extra page ids in feedback"() {
        given:
        def pages = [page(10L), page(20L)]
        def pageMindMap = new PageMindMap()
        pageMindMap.root = new PageMindMapNode(10L, [
                new PageMindMapNode(10L),
                new PageMindMapNode(99L)
        ])

        when:
        def feedback = validator.extractFeedback(pageMindMap, pages)

        then:
        feedback.missingPageIds() == [20L]
        feedback.duplicatePageIds() == [10L]
        feedback.extraPageIds() == [99L]
    }
}
