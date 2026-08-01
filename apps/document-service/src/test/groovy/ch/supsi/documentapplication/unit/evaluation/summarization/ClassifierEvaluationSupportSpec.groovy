package ch.supsi.documentapplication.unit.evaluation.summarization

import ch.supsi.documentapplication.evaluation.summarization.ClassifierEvaluationSupport
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.springframework.core.io.FileSystemResource
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

class ClassifierEvaluationSupportSpec extends Specification {

    @TempDir
    Path tempDir

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()

    def "should load sidecar labels and extract PDF page text"() {
        given:
        def pdfPath = writePdf(tempDir.resolve("book-a.pdf"), ["Cover page", "Lesson body"])
        def sidecarPath = writeSidecar(tempDir.resolve("book-a.labels.json"), """
        {
          "pdf": "book-a.pdf",
          "bookId": "book-a",
          "title": "Book A",
          "source": "test",
          "pages": [
            { "pageNumber": 1, "content": false, "note": "cover" },
            { "pageNumber": 2, "content": true }
          ]
        }
        """)

        when:
        def book = ClassifierEvaluationSupport.loadBookFromFiles(sidecarPath, pdfPath)
        def corpus = ClassifierEvaluationSupport.corpusOf([book])
        ClassifierEvaluationSupport.validateCorpus(corpus)

        then:
        book.pdfFileName == "book-a.pdf"
        book.pages.size() == 2
        !book.pages[0].expectedContent
        book.pages[0].expectedLabel == ClassifierEvaluationSupport.LABEL_NON_CONTENT
        book.pages[0].text.contains("Cover page")
        !book.pages[0].blank
        book.pages[1].expectedContent
        book.pages[1].text.contains("Lesson body")
        corpus.labelPolicy == ClassifierEvaluationSupport.LABEL_POLICY
    }

    def "should mark blank extracted pages without requiring stored text"() {
        given:
        def pdfPath = writePdf(tempDir.resolve("blank-book.pdf"), ["", "Body text"])
        def sidecarPath = writeSidecar(tempDir.resolve("blank-book.labels.json"), """
        {
          "pdf": "blank-book.pdf",
          "bookId": "blank-book",
          "title": "Blank Book",
          "source": "test",
          "pages": [
            { "pageNumber": 1, "content": false },
            { "pageNumber": 2, "content": true }
          ]
        }
        """)

        when:
        def book = ClassifierEvaluationSupport.loadBookFromFiles(sidecarPath, pdfPath)

        then:
        book.pages[0].blank
        book.pages[0].text.isBlank()
        !book.pages[1].blank
    }

    def "should reject incomplete page coverage"() {
        given:
        def pdfPath = writePdf(tempDir.resolve("short.pdf"), ["One", "Two"])
        def sidecarPath = writeSidecar(tempDir.resolve("short.labels.json"), """
        {
          "pdf": "short.pdf",
          "bookId": "short",
          "title": "Short",
          "source": "test",
          "pages": [
            { "pageNumber": 1, "content": true }
          ]
        }
        """)

        when:
        ClassifierEvaluationSupport.loadBookFromFiles(sidecarPath, pdfPath)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("every physical page must be labeled exactly once")
    }

    def "should reject duplicate page numbers"() {
        given:
        def pdfPath = writePdf(tempDir.resolve("dup.pdf"), ["One", "Two"])
        def sidecarPath = writeSidecar(tempDir.resolve("dup.labels.json"), """
        {
          "pdf": "dup.pdf",
          "bookId": "dup",
          "title": "Dup",
          "source": "test",
          "pages": [
            { "pageNumber": 1, "content": true },
            { "pageNumber": 1, "content": false }
          ]
        }
        """)

        when:
        ClassifierEvaluationSupport.loadBookFromFiles(sidecarPath, pdfPath)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("duplicate label")
    }

    def "should reject out-of-range page numbers"() {
        given:
        def pdfPath = writePdf(tempDir.resolve("range.pdf"), ["Only page"])
        def sidecarPath = writeSidecar(tempDir.resolve("range.labels.json"), """
        {
          "pdf": "range.pdf",
          "bookId": "range",
          "title": "Range",
          "source": "test",
          "pages": [
            { "pageNumber": 2, "content": true }
          ]
        }
        """)

        when:
        ClassifierEvaluationSupport.loadBookFromFiles(sidecarPath, pdfPath)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("exceeds PDF page count") || ex.message.contains("every physical page")
    }

    def "should reject empty corpus"() {
        given:
        def corpus = ClassifierEvaluationSupport.corpusOf([])

        when:
        ClassifierEvaluationSupport.validateCorpus(corpus)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("at least one PDF sidecar")
    }

    def "should reject corpus missing a label class"() {
        given:
        def pdfPath = writePdf(tempDir.resolve("only-content.pdf"), ["Body"])
        def sidecarPath = writeSidecar(tempDir.resolve("only-content.labels.json"), """
        {
          "pdf": "only-content.pdf",
          "bookId": "only-content",
          "title": "Only Content",
          "source": "test",
          "pages": [
            { "pageNumber": 1, "content": true }
          ]
        }
        """)
        def book = ClassifierEvaluationSupport.loadBookFromFiles(sidecarPath, pdfPath)
        def corpus = ClassifierEvaluationSupport.corpusOf([book])

        when:
        ClassifierEvaluationSupport.validateCorpus(corpus)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("at least one CONTENT and one NON_CONTENT")
    }

    def "should reject missing adjacent PDF when loading sidecar resource"() {
        given:
        def sidecarPath = writeSidecar(tempDir.resolve("orphan.labels.json"), """
        {
          "pdf": "missing.pdf",
          "bookId": "orphan",
          "title": "Orphan",
          "source": "test",
          "pages": [
            { "pageNumber": 1, "content": true }
          ]
        }
        """)

        when:
        ClassifierEvaluationSupport.loadBookFromSidecar(new FileSystemResource(sidecarPath.toFile()))

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("not found")
    }

    def "should compute confusion counts and zero-denominator metrics"() {
        expect:
        ClassifierEvaluationSupport.countsFrom(true, true).truePositives == 1
        ClassifierEvaluationSupport.countsFrom(false, true).falsePositives == 1
        ClassifierEvaluationSupport.countsFrom(false, false).trueNegatives == 1
        ClassifierEvaluationSupport.countsFrom(true, false).falseNegatives == 1

        and:
        def emptyMetrics = ClassifierEvaluationSupport.metricsFrom(
                new ClassifierEvaluationSupport.ConfusionCounts(0, 0, 0, 0))
        emptyMetrics.precision == 0.0d
        emptyMetrics.recall == 0.0d
        emptyMetrics.f1 == 0.0d

        and:
        def metrics = ClassifierEvaluationSupport.metricsFrom(
                new ClassifierEvaluationSupport.ConfusionCounts(1, 1, 1, 1))
        metrics.precision == 0.5d
        metrics.recall == 0.5d
        metrics.f1 == 0.5d
    }

    def "should aggregate metrics, blank skips, misclassifications, and invocation errors"() {
        given:
        def corpus = sampleCorpus()
        def pages = corpus.allPages()
        def predictions = [
                ClassifierEvaluationSupport.PagePrediction.blankSkipped(pages[0]),
                ClassifierEvaluationSupport.PagePrediction.success(pages[1], false, "", ""),
                ClassifierEvaluationSupport.PagePrediction.success(pages[2], true, "Title", "Summary"),
                ClassifierEvaluationSupport.PagePrediction.failure(pages[3], "timeout")
        ]

        when:
        def report = ClassifierEvaluationSupport.buildReport(corpus, "test-model", predictions)
        Path reportPath = ClassifierEvaluationSupport.writeReport(report, tempDir.resolve("report.json"))
        def written = OBJECT_MAPPER.readValue(Files.readString(reportPath), Map)
        def summary = ClassifierEvaluationSupport.formatSummary(report)

        then:
        report.blankSkippedPages == 1
        report.evaluatedPages == 2
        report.erroredPages == 1
        report.counts.truePositives == 0
        report.counts.falsePositives == 1
        report.counts.trueNegatives == 0
        report.counts.falseNegatives == 1
        report.falsePositives*.pageNumber == [1]
        report.falseNegatives*.pageNumber == [2]
        report.invocationErrors*.pageNumber == [2]
        report.blankPages.size() == 1
        !report.blankPages[0].labelMismatch
        report.blankLabelMismatches.isEmpty()
        written.modelName == "test-model"
        written.blankSkippedPages == 1
        summary.contains("blankSkipped=1")
        summary.contains("falsePositives=[book-b.pdf:1]")
        summary.contains("falseNegatives=[book-a.pdf:2]")
    }

    def "should flag blank pages labeled as content"() {
        given:
        def pdfPath = writePdf(tempDir.resolve("mismatch.pdf"), ["", "Body"])
        def sidecarPath = writeSidecar(tempDir.resolve("mismatch.labels.json"), """
        {
          "pdf": "mismatch.pdf",
          "bookId": "mismatch",
          "title": "Mismatch",
          "source": "test",
          "pages": [
            { "pageNumber": 1, "content": true },
            { "pageNumber": 2, "content": false }
          ]
        }
        """)
        def book = ClassifierEvaluationSupport.loadBookFromFiles(sidecarPath, pdfPath)
        def corpus = ClassifierEvaluationSupport.corpusOf([book])
        def predictions = [
                ClassifierEvaluationSupport.PagePrediction.blankSkipped(book.pages[0]),
                ClassifierEvaluationSupport.PagePrediction.success(book.pages[1], false, "", "")
        ]

        when:
        def report = ClassifierEvaluationSupport.buildReport(corpus, "test-model", predictions)

        then:
        report.blankLabelMismatches.size() == 1
        report.blankLabelMismatches[0].pageNumber == 1
        report.blankLabelMismatches[0].labelMismatch
    }

    private ClassifierEvaluationSupport.Corpus sampleCorpus() {
        def bookAPdf = writePdf(tempDir.resolve("book-a.pdf"), ["", "Lesson body"])
        def bookASidecar = writeSidecar(tempDir.resolve("book-a.labels.json"), """
        {
          "pdf": "book-a.pdf",
          "bookId": "book-a",
          "title": "Book A",
          "source": "test",
          "pages": [
            { "pageNumber": 1, "content": false },
            { "pageNumber": 2, "content": true }
          ]
        }
        """)
        def bookBPdf = writePdf(tempDir.resolve("book-b.pdf"), ["Index", "Another lesson"])
        def bookBSidecar = writeSidecar(tempDir.resolve("book-b.labels.json"), """
        {
          "pdf": "book-b.pdf",
          "bookId": "book-b",
          "title": "Book B",
          "source": "test",
          "pages": [
            { "pageNumber": 1, "content": false },
            { "pageNumber": 2, "content": true }
          ]
        }
        """)
        def books = [
                ClassifierEvaluationSupport.loadBookFromFiles(bookASidecar, bookAPdf),
                ClassifierEvaluationSupport.loadBookFromFiles(bookBSidecar, bookBPdf)
        ]
        def corpus = ClassifierEvaluationSupport.corpusOf(books)
        ClassifierEvaluationSupport.validateCorpus(corpus)
        return corpus
    }

    private static Path writeSidecar(Path path, String json) {
        Files.writeString(path, json.stripIndent().trim())
        return path
    }

    private static Path writePdf(Path path, List<String> pageTexts) {
        def document = new PDDocument()
        pageTexts.each { text ->
            def page = new PDPage()
            document.addPage(page)
            if (text != null && !text.isBlank()) {
                def content = new PDPageContentStream(document, page)
                content.beginText()
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12)
                content.newLineAtOffset(100, 700)
                content.showText(text)
                content.endText()
                content.close()
            }
        }
        document.save(path.toFile())
        document.close()
        return path
    }
}
