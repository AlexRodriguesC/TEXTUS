package ch.supsi.textusbackend.unit.model

import ch.supsi.textusbackend.model.BookIndex
import ch.supsi.textusbackend.model.BookIndexEntry
import org.apache.pdfbox.pdmodel.PDDocument
import spock.lang.Specification

class BookIndexSpec extends Specification {

    private static BookIndexEntry entry(String title, int fromPage, List<BookIndexEntry> children = []) {
        new BookIndexEntry(title, 0, fromPage, 0, children)
    }

    def "should return same instance when index is empty"() {
        given:
        def index = new BookIndex([])
        def pdf = Mock(PDDocument)

        when:
        def result = index.normalise(pdf)

        then:
        result.is(index)
        0 * pdf.getNumberOfPages()
    }

    def "should flatten nested entries in pre-order"() {
        given:
        def index = new BookIndex([
                entry("Unit 1", 8, [
                        entry("Nations", 10),
                        entry("Culture", 14)
                ]),
                entry("Unit 2", 18, [
                        entry("Shopping", 26)
                ])
        ])
        def pdf = Mock(PDDocument) { getNumberOfPages() >> 100 }

        when:
        def result = index.normalise(pdf)

        then:
        result.children()*.title() == ["Unit 1", "Nations", "Culture", "Unit 2", "Shopping"]
        result.children()*.fromPage() == [8, 10, 14, 18, 26]
        result.children()*.position() == [0, 1, 2, 3, 4]
        result.children().every { it.children().isEmpty() }
    }

    def "should assign contiguous zero-based positions"() {
        given:
        def index = new BookIndex([
                entry("A", 2),
                entry("B", 8),
                entry("C", 18)
        ])
        def pdf = Mock(PDDocument) { getNumberOfPages() >> 100 }

        when:
        def result = index.normalise(pdf)

        then:
        result.children()*.position() == [0, 1, 2]
    }

    def "should compute toPage as next entry fromPage minus one"() {
        given:
        def index = new BookIndex([
                entry("A", 2),
                entry("B", 8),
                entry("C", 18)
        ])
        def pdf = Mock(PDDocument) { getNumberOfPages() >> 100 }

        when:
        def result = index.normalise(pdf)

        then:
        result.children()*.fromPage() == [2, 8, 18]
        result.children()*.toPage() == [7, 17, 100]
    }

    def "should set last entry toPage to pdf page count"() {
        given:
        def index = new BookIndex([
                entry("Only section", 5)
        ])
        def pdf = Mock(PDDocument) { getNumberOfPages() >> 42 }

        when:
        def result = index.normalise(pdf)

        then:
        result.children().last().toPage() == 42
    }

    def "should cap fromPage to pdf page count"() {
        given:
        def index = new BookIndex([
                entry("Section A", 90),
                entry("Section B", 95)
        ])
        def pdf = Mock(PDDocument) { getNumberOfPages() >> 92 }

        when:
        def result = index.normalise(pdf)

        then:
        result.children()*.fromPage() == [90, 92]
    }

    def "should drop entries when page numbering restarts"() {
        given:
        def index = new BookIndex([
                entry("Chapter 1", 10),
                entry("Chapter 2", 20),
                entry("Workbook", 2, [
                        entry("Workbook Unit 1", 4)
                ])
        ])
        def pdf = Mock(PDDocument) { getNumberOfPages() >> 100 }

        when:
        def result = index.normalise(pdf)

        then:
        result.children()*.title() == ["Chapter 1", "Chapter 2"]
    }

    def "should drop all entries after a restart including their children"() {
        given:
        def index = new BookIndex([
                entry("A", 5),
                entry("B", 10),
                entry("C", 3, [
                        entry("C1", 5),
                        entry("C2", 8)
                ]),
                entry("D", 15)
        ])
        def pdf = Mock(PDDocument) { getNumberOfPages() >> 100 }

        when:
        def result = index.normalise(pdf)

        then:
        result.children()*.title() == ["A", "B"]
    }

    def "should keep monotonic entries before a restart"() {
        given:
        def index = new BookIndex([
                entry("Starter Unit", 2),
                entry("Unit 1", 8),
                entry("Unit 2", 18),
                entry("Glossary", 86),
                entry("Workbook", 2)
        ])
        def pdf = Mock(PDDocument) { getNumberOfPages() >> 120 }

        when:
        def result = index.normalise(pdf)

        then:
        result.children()*.title() == ["Starter Unit", "Unit 1", "Unit 2", "Glossary"]
        result.children()*.fromPage() == [2, 8, 18, 86]
        result.children().last().toPage() == 120
    }

    def "should satisfy fromPage <= toPage for every entry"() {
        given:
        def index = new BookIndex([
                entry("Unit 1", 8, [
                        entry("Sub A", 10),
                        entry("Sub B", 14)
                ]),
                entry("Unit 2", 18),
                entry("Glossary", 86),
                entry("Workbook", 2)
        ])
        def pdf = Mock(PDDocument) { getNumberOfPages() >> 120 }

        when:
        def result = index.normalise(pdf)

        then:
        result.children().every { it.fromPage() <= it.toPage() }
    }

    def "should handle realistic textbook index dropping workbook"() {
        given:
        def index = new BookIndex([
                entry("Starter Unit - Hello again!", 2, [entry("Revision", 4)]),
                entry("Unit 1 - Crossing cultures", 8, [
                        entry("Nations & nationalities", 10),
                        entry("Culture Entertainment in California", 14),
                        entry("CLIL Native Americans", 16)
                ]),
                entry("Unit 2 - Shops in town", 18, [
                        entry("Shops, items, British currency", 20),
                        entry("Culture Shopping in New York", 26),
                        entry("CLIL The Quechua People", 28)
                ]),
                entry("More English", 80),
                entry("Glossary", 86),
                entry("Workbook", 2, [
                        entry("Starter Unit", 2),
                        entry("Unit 1", 4),
                        entry("Focus on grammar", 46)
                ])
        ])
        def pdf = Mock(PDDocument) { getNumberOfPages() >> 200 }

        when:
        def result = index.normalise(pdf)

        then:
        result.children().every { it.children().isEmpty() }
        result.children()*.title().last() == "Glossary"
        result.children().last().toPage() == 200
        result.children()*.position() == (0..<result.children().size()).toList()
        result.children().every { it.fromPage() <= it.toPage() }
    }
}