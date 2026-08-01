package ch.supsi.documentapplication.unit.evaluation.summarization

import ch.supsi.documentapplication.evaluation.summarization.ClassifierEvaluationSupport
import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

class ClassifierEvaluationSupportSpec extends Specification {

    @TempDir
    Path tempDir

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()

    def "should parse and validate a well-formed multi-book corpus"() {
        given:
        def json = """
        {
          "corpusVersion": "1.0.0",
          "createdAt": "2026-08-01",
          "labelPolicy": "model-invoked-pages-only",
          "books": [
            {
              "bookId": "book-a",
              "title": "Book A",
              "source": "test",
              "pages": [
                {
                  "pageId": "book-a-p1",
                  "pageNumber": 1,
                  "expectedLabel": "NON_CONTENT",
                  "text": "Table of Contents",
                  "note": "toc"
                },
                {
                  "pageId": "book-a-p2",
                  "pageNumber": 2,
                  "expectedLabel": "CONTENT",
                  "text": "Photosynthesis converts light into chemical energy."
                }
              ]
            }
          ]
        }
        """

        when:
        def corpus = ClassifierEvaluationSupport.loadCorpus(new ByteArrayInputStream(json.bytes))
        ClassifierEvaluationSupport.validateCorpus(corpus)

        then:
        corpus.corpusVersion == "1.0.0"
        corpus.books.size() == 1
        corpus.allPages().size() == 2
        corpus.allPages()*.expectedLabel == ["NON_CONTENT", "CONTENT"]
    }

    def "should reject blank page text before any model call"() {
        given:
        def json = """
        {
          "corpusVersion": "1.0.0",
          "createdAt": "2026-08-01",
          "labelPolicy": "model-invoked-pages-only",
          "books": [
            {
              "bookId": "book-a",
              "title": "Book A",
              "source": "test",
              "pages": [
                {
                  "pageId": "book-a-p1",
                  "pageNumber": 1,
                  "expectedLabel": "CONTENT",
                  "text": "   "
                },
                {
                  "pageId": "book-a-p2",
                  "pageNumber": 2,
                  "expectedLabel": "NON_CONTENT",
                  "text": "Index"
                }
              ]
            }
          ]
        }
        """

        when:
        ClassifierEvaluationSupport.loadCorpus(new ByteArrayInputStream(json.bytes))

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("text")
    }

    def "should reject empty books corpus"() {
        given:
        def json = """
        {
          "corpusVersion": "1.0.0",
          "createdAt": "2026-08-01",
          "labelPolicy": "model-invoked-pages-only",
          "books": []
        }
        """
        def corpus = ClassifierEvaluationSupport.loadCorpus(new ByteArrayInputStream(json.bytes))

        when:
        ClassifierEvaluationSupport.validateCorpus(corpus)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("at least one book")
    }

    def "should reject corpus missing a label class"() {
        given:
        def json = """
        {
          "corpusVersion": "1.0.0",
          "createdAt": "2026-08-01",
          "labelPolicy": "model-invoked-pages-only",
          "books": [
            {
              "bookId": "book-a",
              "title": "Book A",
              "source": "test",
              "pages": [
                {
                  "pageId": "book-a-p1",
                  "pageNumber": 1,
                  "expectedLabel": "CONTENT",
                  "text": "Body text"
                }
              ]
            }
          ]
        }
        """
        def corpus = ClassifierEvaluationSupport.loadCorpus(new ByteArrayInputStream(json.bytes))

        when:
        ClassifierEvaluationSupport.validateCorpus(corpus)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("at least one CONTENT and one NON_CONTENT")
    }

    def "should reject duplicate page ids"() {
        given:
        def json = """
        {
          "corpusVersion": "1.0.0",
          "createdAt": "2026-08-01",
          "labelPolicy": "model-invoked-pages-only",
          "books": [
            {
              "bookId": "book-a",
              "title": "Book A",
              "source": "test",
              "pages": [
                {
                  "pageId": "dup",
                  "pageNumber": 1,
                  "expectedLabel": "CONTENT",
                  "text": "Body"
                },
                {
                  "pageId": "dup",
                  "pageNumber": 2,
                  "expectedLabel": "NON_CONTENT",
                  "text": "Index"
                }
              ]
            }
          ]
        }
        """
        def corpus = ClassifierEvaluationSupport.loadCorpus(new ByteArrayInputStream(json.bytes))

        when:
        ClassifierEvaluationSupport.validateCorpus(corpus)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("Duplicate pageId")
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

    def "should aggregate per-book metrics, misclassifications, and invocation errors"() {
        given:
        def corpus = sampleCorpus()
        def pages = corpus.allPages()
        def predictions = [
                ClassifierEvaluationSupport.PagePrediction.success(pages[0], false, "", ""),
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
        report.counts.truePositives == 0
        report.counts.falsePositives == 1
        report.counts.trueNegatives == 1
        report.counts.falseNegatives == 1
        report.metrics.precision == 0.0d
        report.metrics.recall == 0.0d
        report.falsePositives*.pageId == ["book-b-p1"]
        report.falseNegatives*.pageId == ["book-a-p2"]
        report.invocationErrors*.pageId == ["book-b-p2"]
        report.bookReports.size() == 2
        report.evaluatedPages == 3
        report.erroredPages == 1
        written.modelName == "test-model"
        written.counts.falsePositives == 1
        summary.contains("falsePositives=[book-b-p1]")
        summary.contains("falseNegatives=[book-a-p2]")
        summary.contains("invocationErrors=[book-b-p2]")
    }

    def "should load and validate the checked-in curated corpus from classpath"() {
        when:
        def corpus = ClassifierEvaluationSupport.loadCorpusFromClasspath()
        ClassifierEvaluationSupport.validateCorpus(corpus)

        then:
        corpus.labelPolicy == "model-invoked-pages-only"
        corpus.books.size() >= 3
        corpus.allPages().size() >= 100
        corpus.allPages().count { it.expectedLabel == ClassifierEvaluationSupport.LABEL_CONTENT } >= 1
        corpus.allPages().count { it.expectedLabel == ClassifierEvaluationSupport.LABEL_NON_CONTENT } >= 1
        corpus.allPages().every { !it.text.isBlank() }
    }

    private static ClassifierEvaluationSupport.Corpus sampleCorpus() {
        def json = """
        {
          "corpusVersion": "1.0.0",
          "createdAt": "2026-08-01",
          "labelPolicy": "model-invoked-pages-only",
          "books": [
            {
              "bookId": "book-a",
              "title": "Book A",
              "source": "test",
              "pages": [
                {
                  "pageId": "book-a-p1",
                  "pageNumber": 1,
                  "expectedLabel": "NON_CONTENT",
                  "text": "Copyright"
                },
                {
                  "pageId": "book-a-p2",
                  "pageNumber": 2,
                  "expectedLabel": "CONTENT",
                  "text": "Lesson body"
                }
              ]
            },
            {
              "bookId": "book-b",
              "title": "Book B",
              "source": "test",
              "pages": [
                {
                  "pageId": "book-b-p1",
                  "pageNumber": 1,
                  "expectedLabel": "NON_CONTENT",
                  "text": "Index"
                },
                {
                  "pageId": "book-b-p2",
                  "pageNumber": 2,
                  "expectedLabel": "CONTENT",
                  "text": "Another lesson"
                }
              ]
            }
          ]
        }
        """
        def corpus = ClassifierEvaluationSupport.loadCorpus(new ByteArrayInputStream(json.bytes))
        ClassifierEvaluationSupport.validateCorpus(corpus)
        return corpus
    }
}
