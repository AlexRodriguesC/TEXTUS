package ch.supsi.documentapplication.evaluation.summarization

import ch.supsi.documentapplication.service.pdf.PdfPageTextExtractor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

/**
 * Corpus loading, PDF text extraction, confusion-matrix metrics, and report
 * helpers for content-page classifier evaluation against PDF sidecars.
 */
final class ClassifierEvaluationSupport {

    static final String CORPUS_DIR = "classifier-evaluation"
    static final String LABEL_SIDECAR_PATTERN = "classpath*:${CORPUS_DIR}/*.labels.json"
    static final String LABEL_CONTENT = "CONTENT"
    static final String LABEL_NON_CONTENT = "NON_CONTENT"
    static final String LABEL_POLICY = "pdf-sidecar-full-page-coverage"

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)

    private ClassifierEvaluationSupport() {}

    static Corpus loadCorpusFromClasspath(
            String pattern = LABEL_SIDECAR_PATTERN,
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver()) {
        Resource[] resources = resolver.getResources(pattern)
        if (resources == null || resources.length == 0) {
            return new Corpus("0.0.0", Instant.now().toString().substring(0, 10), LABEL_POLICY, List.of())
        }

        List<Book> books = []
        resources.toList().sort { a, b -> a.filename <=> b.filename }.each { Resource resource ->
            books << loadBookFromSidecar(resource)
        }
        return new Corpus("2.0.0", Instant.now().toString().substring(0, 10), LABEL_POLICY, books)
    }

    static Book loadBookFromSidecar(Resource sidecarResource) {
        Objects.requireNonNull(sidecarResource, "sidecarResource")
        if (!sidecarResource.exists()) {
            throw new IllegalArgumentException("Sidecar resource not found: ${sidecarResource.description}")
        }

        Map raw
        sidecarResource.inputStream.withCloseable { stream ->
            raw = OBJECT_MAPPER.readValue(stream, Map)
        }
        if (!(raw instanceof Map)) {
            throw new IllegalArgumentException("Sidecar root must be a JSON object: ${sidecarResource.filename}")
        }

        String pdfFileName = requiredString(raw, "pdf")
        String bookId = requiredString(raw, "bookId")
        String title = requiredString(raw, "title")
        String source = requiredString(raw, "source")
        List<PageLabel> labels = parsePageLabels(raw, sidecarResource.filename)

        Resource pdfResource = resolveAdjacentPdf(sidecarResource, pdfFileName)
        byte[] pdfBytes = pdfResource.inputStream.withCloseable { it.readAllBytes() }
        List<String> extractedTexts = PdfPageTextExtractor.extractPageTexts(pdfBytes)
        validateLabelCoverage(bookId, labels, extractedTexts.size())

        List<CorpusPage> pages = labels.collect { label ->
            String text = extractedTexts.get(label.pageNumber - 1)
            boolean blank = text == null || text.isBlank()
            String expectedLabel = label.content ? LABEL_CONTENT : LABEL_NON_CONTENT
            return new CorpusPage(
                    pageId(bookId, label.pageNumber),
                    bookId,
                    pdfFileName,
                    label.pageNumber,
                    expectedLabel,
                    label.content,
                    text ?: "",
                    blank,
                    label.note)
        }

        return new Book(bookId, title, source, pdfFileName, pages)
    }

    static Book loadBookFromFiles(Path sidecarPath, Path pdfPath) {
        Objects.requireNonNull(sidecarPath, "sidecarPath")
        Objects.requireNonNull(pdfPath, "pdfPath")
        Map raw = OBJECT_MAPPER.readValue(Files.readString(sidecarPath), Map)
        String pdfFileName = requiredString(raw, "pdf")
        String bookId = requiredString(raw, "bookId")
        String title = requiredString(raw, "title")
        String source = requiredString(raw, "source")
        List<PageLabel> labels = parsePageLabels(raw, sidecarPath.fileName.toString())

        byte[] pdfBytes = Files.readAllBytes(pdfPath)
        List<String> extractedTexts = PdfPageTextExtractor.extractPageTexts(pdfBytes)
        validateLabelCoverage(bookId, labels, extractedTexts.size())

        List<CorpusPage> pages = labels.collect { label ->
            String text = extractedTexts.get(label.pageNumber - 1)
            boolean blank = text == null || text.isBlank()
            String expectedLabel = label.content ? LABEL_CONTENT : LABEL_NON_CONTENT
            return new CorpusPage(
                    pageId(bookId, label.pageNumber),
                    bookId,
                    pdfFileName,
                    label.pageNumber,
                    expectedLabel,
                    label.content,
                    text ?: "",
                    blank,
                    label.note)
        }

        return new Book(bookId, title, source, pdfFileName, pages)
    }

    static Corpus corpusOf(List<Book> books) {
        return new Corpus("2.0.0", Instant.now().toString().substring(0, 10), LABEL_POLICY, books)
    }

    static void validateCorpus(Corpus corpus) {
        Objects.requireNonNull(corpus, "corpus")
        if (corpus.books.isEmpty()) {
            throw new IllegalArgumentException(
                    "Corpus must contain at least one PDF sidecar pair under ${CORPUS_DIR}/ " +
                            "(*.pdf + *.labels.json).")
        }

        Set<String> bookIds = new HashSet<>()
        Set<String> pageIds = new HashSet<>()
        int contentCount = 0
        int nonContentCount = 0

        corpus.books.each { book ->
            if (!bookIds.add(book.bookId)) {
                throw new IllegalArgumentException("Duplicate bookId: ${book.bookId}")
            }
            if (book.pages.isEmpty()) {
                throw new IllegalArgumentException("Book '${book.bookId}' must contain at least one page")
            }
            book.pages.each { page ->
                if (!pageIds.add(page.pageId)) {
                    throw new IllegalArgumentException("Duplicate pageId: ${page.pageId}")
                }
                if (page.bookId != book.bookId) {
                    throw new IllegalArgumentException(
                            "Page '${page.pageId}' bookId '${page.bookId}' does not match parent '${book.bookId}'")
                }
                if (page.expectedContent) {
                    contentCount++
                } else {
                    nonContentCount++
                }
            }
        }

        if (contentCount < 1 || nonContentCount < 1) {
            throw new IllegalArgumentException(
                    "Corpus must contain at least one CONTENT and one NON_CONTENT page " +
                            "(found CONTENT=${contentCount}, NON_CONTENT=${nonContentCount})")
        }
    }

    static ConfusionCounts countsFrom(boolean expectedContent, boolean predictedContent) {
        if (expectedContent && predictedContent) {
            return new ConfusionCounts(1, 0, 0, 0)
        }
        if (!expectedContent && predictedContent) {
            return new ConfusionCounts(0, 1, 0, 0)
        }
        if (!expectedContent && !predictedContent) {
            return new ConfusionCounts(0, 0, 1, 0)
        }
        return new ConfusionCounts(0, 0, 0, 1)
    }

    static Metrics metricsFrom(ConfusionCounts counts) {
        double precision = ratio(counts.truePositives, counts.truePositives + counts.falsePositives)
        double recall = ratio(counts.truePositives, counts.truePositives + counts.falseNegatives)
        double f1 = (precision + recall) == 0.0d ? 0.0d : (2.0d * precision * recall) / (precision + recall)
        return new Metrics(precision, recall, f1)
    }

    static EvaluationReport buildReport(
            Corpus corpus,
            String modelName,
            List<PagePrediction> predictions,
            Instant timestamp = Instant.now()) {
        Map<String, List<PagePrediction>> byBook = predictions.groupBy { it.page.bookId }
        List<BookReport> bookReports = corpus.books.collect { book ->
            List<PagePrediction> bookPredictions = byBook.getOrDefault(book.bookId, List.of())
            aggregateBook(book, bookPredictions)
        }

        ConfusionCounts overallCounts = bookReports.inject(new ConfusionCounts(0, 0, 0, 0)) { acc, bookReport ->
            acc.plus(bookReport.counts)
        }
        Metrics overallMetrics = metricsFrom(overallCounts)
        List<Misclassification> falsePositives = bookReports.collectMany { it.falsePositives }
        List<Misclassification> falseNegatives = bookReports.collectMany { it.falseNegatives }
        List<InvocationError> invocationErrors = bookReports.collectMany { it.invocationErrors }
        List<BlankPageResult> blankPages = bookReports.collectMany { it.blankPages }
        List<BlankPageResult> blankLabelMismatches = blankPages.findAll { it.labelMismatch }

        int totalPages = predictions.size()
        int blankSkipped = predictions.count { it.blankSkipped }
        int evaluatedPages = predictions.count { !it.blankSkipped && it.errorMessage == null }
        int erroredPages = predictions.count { it.errorMessage != null }

        return new EvaluationReport(
                corpus.corpusVersion,
                corpus.labelPolicy,
                modelName,
                timestamp.toString(),
                totalPages,
                evaluatedPages,
                blankSkipped,
                erroredPages,
                overallCounts,
                overallMetrics,
                falsePositives,
                falseNegatives,
                invocationErrors,
                blankPages,
                blankLabelMismatches,
                bookReports)
    }

    static String formatSummary(EvaluationReport report) {
        def sb = new StringBuilder()
        sb.append("Content-page classifier evaluation\n")
        sb.append("corpusVersion=${report.corpusVersion} model=${report.modelName} " +
                "pages=${report.totalPages} evaluated=${report.evaluatedPages} " +
                "blankSkipped=${report.blankSkippedPages} errors=${report.erroredPages}\n")
        sb.append(formatCounts("overall", report.counts, report.metrics))
        report.bookReports.each { book ->
            sb.append(formatCounts("book ${book.bookId} (${book.pdfFileName})", book.counts, book.metrics))
            if (book.blankPages) {
                sb.append("  blankPages=${book.blankPages.collect { it.pageNumber }}\n")
            }
        }
        if (!report.falsePositives.isEmpty()) {
            sb.append("falsePositives=${report.falsePositives.collect { "${it.pdfFileName}:${it.pageNumber}" }}\n")
        }
        if (!report.falseNegatives.isEmpty()) {
            sb.append("falseNegatives=${report.falseNegatives.collect { "${it.pdfFileName}:${it.pageNumber}" }}\n")
        }
        if (!report.blankLabelMismatches.isEmpty()) {
            sb.append("blankLabelMismatches=${report.blankLabelMismatches.collect { "${it.pdfFileName}:${it.pageNumber}" }}\n")
        }
        if (!report.invocationErrors.isEmpty()) {
            sb.append("invocationErrors=${report.invocationErrors.collect { "${it.pdfFileName}:${it.pageNumber}" }}\n")
        }
        return sb.toString()
    }

    static Path writeReport(EvaluationReport report, Path outputPath) {
        Files.createDirectories(outputPath.parent)
        Files.writeString(outputPath, OBJECT_MAPPER.writeValueAsString(report.toMap()))
        return outputPath
    }

    static Path defaultReportPath(Path projectDir = Path.of(".").toAbsolutePath().normalize()) {
        return projectDir.resolve("build/reports/classifier-evaluation/report.json")
    }

    private static BookReport aggregateBook(Book book, List<PagePrediction> predictions) {
        ConfusionCounts counts = new ConfusionCounts(0, 0, 0, 0)
        List<Misclassification> falsePositives = []
        List<Misclassification> falseNegatives = []
        List<InvocationError> invocationErrors = []
        List<BlankPageResult> blankPages = []

        predictions.each { prediction ->
            if (prediction.blankSkipped) {
                boolean labelMismatch = prediction.page.expectedContent
                blankPages << new BlankPageResult(
                        prediction.page.bookId,
                        prediction.page.pdfFileName,
                        prediction.page.pageId,
                        prediction.page.pageNumber,
                        prediction.page.expectedLabel,
                        labelMismatch)
                return
            }

            if (prediction.errorMessage != null) {
                invocationErrors << new InvocationError(
                        prediction.page.bookId,
                        prediction.page.pdfFileName,
                        prediction.page.pageId,
                        prediction.page.pageNumber,
                        prediction.errorMessage)
                return
            }

            boolean expectedContent = prediction.page.expectedContent
            boolean predictedContent = prediction.predictedLabel == LABEL_CONTENT
            counts = counts.plus(countsFrom(expectedContent, predictedContent))

            if (!expectedContent && predictedContent) {
                falsePositives << new Misclassification(
                        prediction.page.bookId,
                        prediction.page.pdfFileName,
                        prediction.page.pageId,
                        prediction.page.pageNumber,
                        prediction.page.expectedLabel,
                        prediction.predictedLabel,
                        prediction.title,
                        prediction.summary)
            } else if (expectedContent && !predictedContent) {
                falseNegatives << new Misclassification(
                        prediction.page.bookId,
                        prediction.page.pdfFileName,
                        prediction.page.pageId,
                        prediction.page.pageNumber,
                        prediction.page.expectedLabel,
                        prediction.predictedLabel,
                        prediction.title,
                        prediction.summary)
            }
        }

        return new BookReport(
                book.bookId,
                book.title,
                book.pdfFileName,
                predictions.size(),
                counts,
                metricsFrom(counts),
                falsePositives,
                falseNegatives,
                invocationErrors,
                blankPages)
    }

    private static Resource resolveAdjacentPdf(Resource sidecarResource, String pdfFileName) {
        String sidecarUrl = sidecarResource.URL.toString()
        String pdfUrl = sidecarUrl.replace(sidecarResource.filename, pdfFileName)
        Resource pdfResource = new PathMatchingResourcePatternResolver().getResource(pdfUrl)
        if (!pdfResource.exists()) {
            // Fall back to classpath relative path under classifier-evaluation/
            pdfResource = new PathMatchingResourcePatternResolver()
                    .getResource("classpath:${CORPUS_DIR}/${pdfFileName}")
        }
        if (!pdfResource.exists()) {
            throw new IllegalArgumentException(
                    "PDF '${pdfFileName}' not found adjacent to sidecar '${sidecarResource.filename}'")
        }
        return pdfResource
    }

    private static List<PageLabel> parsePageLabels(Map raw, String sidecarName) {
        def pagesRaw = raw.pages
        if (!(pagesRaw instanceof List)) {
            throw new IllegalArgumentException("Sidecar '${sidecarName}' field 'pages' must be an array")
        }
        if (pagesRaw.isEmpty()) {
            throw new IllegalArgumentException("Sidecar '${sidecarName}' must contain at least one page label")
        }

        List<PageLabel> labels = []
        pagesRaw.eachWithIndex { pageRaw, index ->
            if (!(pageRaw instanceof Map)) {
                throw new IllegalArgumentException("Sidecar '${sidecarName}' pages[${index}] must be an object")
            }
            Map pageMap = pageRaw as Map
            Object pageNumberRaw = pageMap.pageNumber
            if (!(pageNumberRaw instanceof Number)) {
                throw new IllegalArgumentException(
                        "Sidecar '${sidecarName}' pages[${index}].pageNumber must be a number")
            }
            int pageNumber = (pageNumberRaw as Number).intValue()
            if (pageNumber < 1) {
                throw new IllegalArgumentException(
                        "Sidecar '${sidecarName}' pages[${index}].pageNumber must be >= 1")
            }
            Object contentRaw = pageMap.content
            if (!(contentRaw instanceof Boolean)) {
                throw new IllegalArgumentException(
                        "Sidecar '${sidecarName}' pages[${index}].content must be a boolean")
            }
            String note = pageMap.note == null ? null : String.valueOf(pageMap.note)
            labels << new PageLabel(pageNumber, contentRaw as boolean, note)
        }
        return labels
    }

    private static void validateLabelCoverage(String bookId, List<PageLabel> labels, int pdfPageCount) {
        if (labels.size() != pdfPageCount) {
            throw new IllegalArgumentException(
                    "Book '${bookId}' has ${labels.size()} labels but PDF has ${pdfPageCount} pages; " +
                            "every physical page must be labeled exactly once")
        }

        Set<Integer> seen = new HashSet<>()
        labels.each { label ->
            if (label.pageNumber > pdfPageCount) {
                throw new IllegalArgumentException(
                        "Book '${bookId}' label pageNumber ${label.pageNumber} exceeds PDF page count ${pdfPageCount}")
            }
            if (!seen.add(label.pageNumber)) {
                throw new IllegalArgumentException(
                        "Book '${bookId}' has duplicate label for pageNumber ${label.pageNumber}")
            }
        }

        for (int page = 1; page <= pdfPageCount; page++) {
            if (!seen.contains(page)) {
                throw new IllegalArgumentException(
                        "Book '${bookId}' is missing a label for pageNumber ${page}")
            }
        }
    }

    private static String pageId(String bookId, int pageNumber) {
        return "${bookId}-p${String.format(Locale.ROOT, '%03d', pageNumber)}"
    }

    private static String requiredString(Map raw, String field) {
        Object value = raw.get(field)
        if (value == null) {
            throw new IllegalArgumentException("Missing required field '${field}'")
        }
        String text = String.valueOf(value).trim()
        if (text.isEmpty() || text == "null") {
            throw new IllegalArgumentException("Field '${field}' must be a non-empty string")
        }
        return text
    }

    private static double ratio(int numerator, int denominator) {
        return denominator == 0 ? 0.0d : (numerator as double) / (denominator as double)
    }

    private static String formatCounts(String label, ConfusionCounts counts, Metrics metrics) {
        return String.format(
                Locale.ROOT,
                "%s: tp=%d fp=%d tn=%d fn=%d precision=%.4f recall=%.4f f1=%.4f%n",
                label,
                counts.truePositives,
                counts.falsePositives,
                counts.trueNegatives,
                counts.falseNegatives,
                metrics.precision,
                metrics.recall,
                metrics.f1)
    }

    private static final class PageLabel {
        final int pageNumber
        final boolean content
        final String note

        PageLabel(int pageNumber, boolean content, String note) {
            this.pageNumber = pageNumber
            this.content = content
            this.note = note
        }
    }

    static final class Corpus {
        final String corpusVersion
        final String createdAt
        final String labelPolicy
        final List<Book> books

        Corpus(String corpusVersion, String createdAt, String labelPolicy, List<Book> books) {
            this.corpusVersion = corpusVersion
            this.createdAt = createdAt
            this.labelPolicy = labelPolicy
            this.books = List.copyOf(books)
        }

        List<CorpusPage> allPages() {
            return books.collectMany { it.pages }
        }
    }

    static final class Book {
        final String bookId
        final String title
        final String source
        final String pdfFileName
        final List<CorpusPage> pages

        Book(String bookId, String title, String source, String pdfFileName, List<CorpusPage> pages) {
            this.bookId = bookId
            this.title = title
            this.source = source
            this.pdfFileName = pdfFileName
            this.pages = List.copyOf(pages)
        }
    }

    static final class CorpusPage {
        final String pageId
        final String bookId
        final String pdfFileName
        final int pageNumber
        final String expectedLabel
        final boolean expectedContent
        final String text
        final boolean blank
        final String note

        CorpusPage(
                String pageId,
                String bookId,
                String pdfFileName,
                int pageNumber,
                String expectedLabel,
                boolean expectedContent,
                String text,
                boolean blank,
                String note) {
            this.pageId = pageId
            this.bookId = bookId
            this.pdfFileName = pdfFileName
            this.pageNumber = pageNumber
            this.expectedLabel = expectedLabel
            this.expectedContent = expectedContent
            this.text = text
            this.blank = blank
            this.note = note
        }
    }

    static final class ConfusionCounts {
        final int truePositives
        final int falsePositives
        final int trueNegatives
        final int falseNegatives

        ConfusionCounts(int truePositives, int falsePositives, int trueNegatives, int falseNegatives) {
            this.truePositives = truePositives
            this.falsePositives = falsePositives
            this.trueNegatives = trueNegatives
            this.falseNegatives = falseNegatives
        }

        ConfusionCounts plus(ConfusionCounts other) {
            return new ConfusionCounts(
                    truePositives + other.truePositives,
                    falsePositives + other.falsePositives,
                    trueNegatives + other.trueNegatives,
                    falseNegatives + other.falseNegatives)
        }

        Map<String, Integer> toMap() {
            return [
                    truePositives : truePositives,
                    falsePositives: falsePositives,
                    trueNegatives : trueNegatives,
                    falseNegatives: falseNegatives
            ]
        }
    }

    static final class Metrics {
        final double precision
        final double recall
        final double f1

        Metrics(double precision, double recall, double f1) {
            this.precision = precision
            this.recall = recall
            this.f1 = f1
        }

        Map<String, Double> toMap() {
            return [precision: precision, recall: recall, f1: f1]
        }
    }

    static final class PagePrediction {
        final CorpusPage page
        final String predictedLabel
        final String title
        final String summary
        final String errorMessage
        final boolean blankSkipped

        PagePrediction(
                CorpusPage page,
                String predictedLabel,
                String title,
                String summary,
                String errorMessage,
                boolean blankSkipped) {
            this.page = page
            this.predictedLabel = predictedLabel
            this.title = title
            this.summary = summary
            this.errorMessage = errorMessage
            this.blankSkipped = blankSkipped
        }

        static PagePrediction success(CorpusPage page, boolean contentPage, String title, String summary) {
            return new PagePrediction(
                    page,
                    contentPage ? LABEL_CONTENT : LABEL_NON_CONTENT,
                    title ?: "",
                    summary ?: "",
                    null,
                    false)
        }

        static PagePrediction blankSkipped(CorpusPage page) {
            return new PagePrediction(page, null, null, null, null, true)
        }

        static PagePrediction failure(CorpusPage page, String errorMessage) {
            return new PagePrediction(page, null, null, null, errorMessage, false)
        }
    }

    static final class Misclassification {
        final String bookId
        final String pdfFileName
        final String pageId
        final int pageNumber
        final String expectedLabel
        final String predictedLabel
        final String title
        final String summary

        Misclassification(
                String bookId,
                String pdfFileName,
                String pageId,
                int pageNumber,
                String expectedLabel,
                String predictedLabel,
                String title,
                String summary) {
            this.bookId = bookId
            this.pdfFileName = pdfFileName
            this.pageId = pageId
            this.pageNumber = pageNumber
            this.expectedLabel = expectedLabel
            this.predictedLabel = predictedLabel
            this.title = title
            this.summary = summary
        }

        Map<String, Object> toMap() {
            return [
                    bookId         : bookId,
                    pdfFileName    : pdfFileName,
                    pageId         : pageId,
                    pageNumber     : pageNumber,
                    expectedLabel  : expectedLabel,
                    predictedLabel : predictedLabel,
                    title          : title,
                    summary        : summary
            ]
        }
    }

    static final class InvocationError {
        final String bookId
        final String pdfFileName
        final String pageId
        final int pageNumber
        final String message

        InvocationError(String bookId, String pdfFileName, String pageId, int pageNumber, String message) {
            this.bookId = bookId
            this.pdfFileName = pdfFileName
            this.pageId = pageId
            this.pageNumber = pageNumber
            this.message = message
        }

        Map<String, Object> toMap() {
            return [
                    bookId     : bookId,
                    pdfFileName: pdfFileName,
                    pageId     : pageId,
                    pageNumber : pageNumber,
                    message    : message
            ]
        }
    }

    static final class BlankPageResult {
        final String bookId
        final String pdfFileName
        final String pageId
        final int pageNumber
        final String expectedLabel
        final boolean labelMismatch

        BlankPageResult(
                String bookId,
                String pdfFileName,
                String pageId,
                int pageNumber,
                String expectedLabel,
                boolean labelMismatch) {
            this.bookId = bookId
            this.pdfFileName = pdfFileName
            this.pageId = pageId
            this.pageNumber = pageNumber
            this.expectedLabel = expectedLabel
            this.labelMismatch = labelMismatch
        }

        Map<String, Object> toMap() {
            return [
                    bookId        : bookId,
                    pdfFileName   : pdfFileName,
                    pageId        : pageId,
                    pageNumber    : pageNumber,
                    expectedLabel : expectedLabel,
                    labelMismatch : labelMismatch
            ]
        }
    }

    static final class BookReport {
        final String bookId
        final String title
        final String pdfFileName
        final int pageCount
        final ConfusionCounts counts
        final Metrics metrics
        final List<Misclassification> falsePositives
        final List<Misclassification> falseNegatives
        final List<InvocationError> invocationErrors
        final List<BlankPageResult> blankPages

        BookReport(
                String bookId,
                String title,
                String pdfFileName,
                int pageCount,
                ConfusionCounts counts,
                Metrics metrics,
                List<Misclassification> falsePositives,
                List<Misclassification> falseNegatives,
                List<InvocationError> invocationErrors,
                List<BlankPageResult> blankPages) {
            this.bookId = bookId
            this.title = title
            this.pdfFileName = pdfFileName
            this.pageCount = pageCount
            this.counts = counts
            this.metrics = metrics
            this.falsePositives = List.copyOf(falsePositives)
            this.falseNegatives = List.copyOf(falseNegatives)
            this.invocationErrors = List.copyOf(invocationErrors)
            this.blankPages = List.copyOf(blankPages)
        }

        Map<String, Object> toMap() {
            return [
                    bookId           : bookId,
                    title            : title,
                    pdfFileName      : pdfFileName,
                    pageCount        : pageCount,
                    counts           : counts.toMap(),
                    metrics          : metrics.toMap(),
                    falsePositives   : falsePositives.collect { it.toMap() },
                    falseNegatives   : falseNegatives.collect { it.toMap() },
                    invocationErrors : invocationErrors.collect { it.toMap() },
                    blankPages       : blankPages.collect { it.toMap() }
            ]
        }
    }

    static final class EvaluationReport {
        final String corpusVersion
        final String labelPolicy
        final String modelName
        final String timestamp
        final int totalPages
        final int evaluatedPages
        final int blankSkippedPages
        final int erroredPages
        final ConfusionCounts counts
        final Metrics metrics
        final List<Misclassification> falsePositives
        final List<Misclassification> falseNegatives
        final List<InvocationError> invocationErrors
        final List<BlankPageResult> blankPages
        final List<BlankPageResult> blankLabelMismatches
        final List<BookReport> bookReports

        EvaluationReport(
                String corpusVersion,
                String labelPolicy,
                String modelName,
                String timestamp,
                int totalPages,
                int evaluatedPages,
                int blankSkippedPages,
                int erroredPages,
                ConfusionCounts counts,
                Metrics metrics,
                List<Misclassification> falsePositives,
                List<Misclassification> falseNegatives,
                List<InvocationError> invocationErrors,
                List<BlankPageResult> blankPages,
                List<BlankPageResult> blankLabelMismatches,
                List<BookReport> bookReports) {
            this.corpusVersion = corpusVersion
            this.labelPolicy = labelPolicy
            this.modelName = modelName
            this.timestamp = timestamp
            this.totalPages = totalPages
            this.evaluatedPages = evaluatedPages
            this.blankSkippedPages = blankSkippedPages
            this.erroredPages = erroredPages
            this.counts = counts
            this.metrics = metrics
            this.falsePositives = List.copyOf(falsePositives)
            this.falseNegatives = List.copyOf(falseNegatives)
            this.invocationErrors = List.copyOf(invocationErrors)
            this.blankPages = List.copyOf(blankPages)
            this.blankLabelMismatches = List.copyOf(blankLabelMismatches)
            this.bookReports = List.copyOf(bookReports)
        }

        Map<String, Object> toMap() {
            return [
                    corpusVersion         : corpusVersion,
                    labelPolicy           : labelPolicy,
                    modelName             : modelName,
                    timestamp             : timestamp,
                    totalPages            : totalPages,
                    evaluatedPages        : evaluatedPages,
                    blankSkippedPages     : blankSkippedPages,
                    erroredPages          : erroredPages,
                    counts                : counts.toMap(),
                    metrics               : metrics.toMap(),
                    falsePositives        : falsePositives.collect { it.toMap() },
                    falseNegatives        : falseNegatives.collect { it.toMap() },
                    invocationErrors      : invocationErrors.collect { it.toMap() },
                    blankPages            : blankPages.collect { it.toMap() },
                    blankLabelMismatches  : blankLabelMismatches.collect { it.toMap() },
                    books                 : bookReports.collect { it.toMap() }
            ]
        }
    }
}
