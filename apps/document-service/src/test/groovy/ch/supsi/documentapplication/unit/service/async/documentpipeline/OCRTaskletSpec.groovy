package ch.supsi.documentapplication.unit.service.async.documentpipeline

import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage
import ch.supsi.documentapplication.model.FsDocument
import ch.supsi.documentapplication.model.PageChunk
import ch.supsi.documentapplication.model.TaskType
import ch.supsi.documentapplication.service.async.documentpipeline.OCRTasklet
import ch.supsi.documentapplication.service.async.documentpipeline.OcrPageProcessor
import ch.supsi.documentapplication.service.async.documentpipeline.SetupTasklet
import ch.supsi.documentapplication.service.document.DocumentTaskProgressService
import ch.supsi.documentapplication.unit.service.async.TaskletTestSupport
import com.github.pemistahl.lingua.api.LanguageDetector
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class OCRTaskletSpec extends Specification {

    IFileSystemDocumentStorage documentStorage = Mock()
    LanguageDetector languageDetector = Mock()
    DocumentTaskProgressService documentTaskProgressService = Mock()
    OcrPageProcessor ocrPageProcessor = Mock()
    AppProperties appProperties = new AppProperties()
    StepContribution stepContribution = Mock()
    OCRTasklet ocrTasklet

    UUID documentId = UUID.randomUUID()
    Long databaseDocumentId = 42L

    def setup() {
        appProperties.maxParallelism = 2
        appProperties.maxChunksPerStep = 5
        ocrTasklet = new OCRTasklet(
                documentStorage,
                languageDetector,
                documentTaskProgressService,
                ocrPageProcessor,
                appProperties)
        ReflectionTestUtils.setField(ocrTasklet, "documentId", documentId.toString())
        ReflectionTestUtils.setField(ocrTasklet, "spaceId", 1L)
    }

    def "should skip OCR and complete task when all chunks have sufficient text"() {
        given:
        def chunks = [new PageChunk(1, sufficientText())]
        def chunkContext = TaskletTestSupport.chunkContext(
                [(SetupTasklet.DATABASE_DOCUMENT_ID): databaseDocumentId])

        when:
        RepeatStatus status = ocrTasklet.execute(stepContribution, chunkContext)

        then:
        1 * documentStorage.listChunks(documentId, Integer.MAX_VALUE) >> chunks
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.OCR, 1)
        2 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.OCR)
        0 * documentStorage.read(_)
        0 * ocrPageProcessor._
        0 * documentStorage.createChunk(_, _, _)
        status == RepeatStatus.FINISHED
    }

    def "should return CONTINUABLE and overwrite only short chunks when batch limit not reached"() {
        given:
        def chunks = mutableChunks(
                new PageChunk(1, sufficientText()),
                new PageChunk(2, "short"),
                new PageChunk(3, ""),
                new PageChunk(4, "x"))
        def pdfBytes = multiPagePdfBytes(4)
        def chunkContext = chunkContextWithFreshStepContext(databaseDocumentId)
        appProperties.maxChunksPerStep = 2

        when:
        RepeatStatus status = ocrTasklet.execute(stepContribution, chunkContext)

        then:
        1 * documentStorage.read(documentId) >> FsDocument.create(documentId, pdfBytes)
        _ * documentStorage.listChunks(documentId, Integer.MAX_VALUE) >> { chunksSnapshot(chunks) }
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.OCR, 3)
        2 * ocrPageProcessor.extractText(_, "eng") >> ocrText()
        2 * documentStorage.createChunk(_, ocrText(), documentId) >> { int page, String text, UUID docId ->
            overwriteChunk(chunks, page, text)
            String.valueOf(page)
        }
        1 * documentTaskProgressService.updateProgress(databaseDocumentId, TaskType.OCR, 1)
        1 * documentTaskProgressService.updateProgress(databaseDocumentId, TaskType.OCR, 2)
        0 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.OCR)
        status == RepeatStatus.CONTINUABLE
    }

    def "should complete task before FINISHED when all pages needing OCR are processed"() {
        given:
        def chunks = mutableChunks(
                new PageChunk(1, sufficientText()),
                new PageChunk(2, "short"))
        def pdfBytes = multiPagePdfBytes(2)
        def chunkContext = chunkContextWithFreshStepContext(databaseDocumentId)
        appProperties.maxChunksPerStep = 5

        when:
        RepeatStatus status = ocrTasklet.execute(stepContribution, chunkContext)

        then:
        1 * documentStorage.read(documentId) >> FsDocument.create(documentId, pdfBytes)
        _ * documentStorage.listChunks(documentId, Integer.MAX_VALUE) >> { chunksSnapshot(chunks) }
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.OCR, 1)
        1 * ocrPageProcessor.extractText(_, "eng") >> ocrText()
        1 * documentStorage.createChunk(2, ocrText(), documentId) >> { int page, String text, UUID docId ->
            overwriteChunk(chunks, page, text)
            "2"
        }
        1 * documentTaskProgressService.updateProgress(databaseDocumentId, TaskType.OCR, 1)
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.OCR)
        status == RepeatStatus.FINISHED
    }

    def "should resume and process remaining pages on next invocation"() {
        given:
        def chunks = mutableChunks(
                new PageChunk(1, ocrText()),
                new PageChunk(2, "tiny"))
        def pdfBytes = multiPagePdfBytes(2)
        def stepExecutionContext = new ExecutionContext()
        stepExecutionContext.put("OCR_TASK_STARTED", true)
        stepExecutionContext.putInt("OCR_PROGRESS", 1)
        def chunkContext = TaskletTestSupport.chunkContext(
                [(SetupTasklet.DATABASE_DOCUMENT_ID): databaseDocumentId],
                stepExecutionContext)
        appProperties.maxChunksPerStep = 1

        when:
        RepeatStatus status = ocrTasklet.execute(stepContribution, chunkContext)

        then:
        1 * documentStorage.read(documentId) >> FsDocument.create(documentId, pdfBytes)
        _ * documentStorage.listChunks(documentId, Integer.MAX_VALUE) >> { chunksSnapshot(chunks) }
        0 * documentTaskProgressService.startTask(_, _, _)
        1 * ocrPageProcessor.extractText(_, "eng") >> ocrText()
        1 * documentStorage.createChunk(2, ocrText(), documentId) >> { int page, String text, UUID docId ->
            overwriteChunk(chunks, page, text)
            "2"
        }
        1 * documentTaskProgressService.updateProgress(databaseDocumentId, TaskType.OCR, 2)
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.OCR)
        status == RepeatStatus.FINISHED
    }

    private static String sufficientText() {
        "This page contains more than forty characters of extractable text."
    }

    private static String ocrText() {
        "OCR extracted text that is definitely longer than forty characters."
    }

    private static List<PageChunk> mutableChunks(PageChunk... initialChunks) {
        new ArrayList<>(initialChunks.toList())
    }

    private static List<PageChunk> chunksSnapshot(List<PageChunk> chunks) {
        chunks.findAll { it.content().trim().length() < 40 }
    }

    private static void overwriteChunk(List<PageChunk> chunks, int pageNumber, String text) {
        def index = chunks.findIndexOf { it.pageNumber() == pageNumber }
        if (index >= 0) {
            chunks[index] = new PageChunk(pageNumber, text)
        }
    }

    private static chunkContextWithFreshStepContext(Long databaseDocumentId) {
        TaskletTestSupport.chunkContext(
                [(SetupTasklet.DATABASE_DOCUMENT_ID): databaseDocumentId],
                new ExecutionContext())
    }

    private static byte[] multiPagePdfBytes(int pageCount) {
        def document = new PDDocument()
        (1..pageCount).each { _ ->
            document.addPage(new PDPage())
        }
        def output = new ByteArrayOutputStream()
        document.save(output)
        document.close()
        return output.toByteArray()
    }

    private static byte[] textPdfBytes(String text) {
        def document = new PDDocument()
        document.addPage(new PDPage())
        def content = new PDPageContentStream(document, document.getPage(0))
        content.beginText()
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12)
        content.newLineAtOffset(50, 700)
        content.showText(text)
        content.endText()
        content.close()
        def output = new ByteArrayOutputStream()
        document.save(output)
        document.close()
        return output.toByteArray()
    }
}
