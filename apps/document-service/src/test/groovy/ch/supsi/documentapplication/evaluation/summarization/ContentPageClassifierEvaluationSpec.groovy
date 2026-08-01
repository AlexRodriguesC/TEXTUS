package ch.supsi.documentapplication.evaluation.summarization

import ch.supsi.documentapplication.service.clients.OpenAISummarizationClient
import spock.lang.Specification

import java.nio.file.Path

/**
 * Opted-in live evaluation of content vs non-content page classification.
 * Invoked via {@code ./gradlew classifierEvaluationTest}; excluded from {@code test}.
 *
 * <p>Loads every {@code *.labels.json} sidecar under {@code classifier-evaluation/},
 * extracts raw PDFBox text for each physical page, skips blank pages before the model
 * call, and compares predictions with ground truth.
 */
class ContentPageClassifierEvaluationSpec extends Specification {

    def "evaluate content-page classifier against PDF sidecar corpus"() {
        given:
        def corpus = ClassifierEvaluationSupport.loadCorpusFromClasspath()
        ClassifierEvaluationSupport.validateCorpus(corpus)

        def clientContext = SummarizationClientEvaluationConfiguration.create()
        OpenAISummarizationClient client = clientContext.client

        when:
        List<ClassifierEvaluationSupport.PagePrediction> predictions = corpus.allPages().collect { page ->
            if (page.blank) {
                return ClassifierEvaluationSupport.PagePrediction.blankSkipped(page)
            }
            try {
                def result = client.getResult(page.text)
                def summarization = result.result()
                return ClassifierEvaluationSupport.PagePrediction.success(
                        page,
                        summarization.contentPage,
                        summarization.title,
                        summarization.summary)
            } catch (Exception ex) {
                return ClassifierEvaluationSupport.PagePrediction.failure(
                        page,
                        "${ex.getClass().getSimpleName()}: ${ex.message}")
            }
        }

        def report = ClassifierEvaluationSupport.buildReport(
                corpus,
                clientContext.modelName,
                predictions)
        Path reportPath = ClassifierEvaluationSupport.writeReport(
                report,
                ClassifierEvaluationSupport.defaultReportPath())
        println ClassifierEvaluationSupport.formatSummary(report)
        println "Wrote classifier evaluation report to ${reportPath}"

        then:
        report.invocationErrors.isEmpty()
        report.blankLabelMismatches.isEmpty()
    }
}
