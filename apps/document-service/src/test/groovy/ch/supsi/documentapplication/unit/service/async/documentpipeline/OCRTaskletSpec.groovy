package ch.supsi.documentapplication.unit.service.async.documentpipeline

import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage
import ch.supsi.documentapplication.model.FsDocument
import ch.supsi.documentapplication.model.TaskType
import ch.supsi.documentapplication.service.async.documentpipeline.OCRTasklet
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
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class OCRTaskletSpec extends Specification {

    IFileSystemDocumentStorage documentStorage = Mock()
    LanguageDetector languageDetector = Mock()
    DocumentTaskProgressService documentTaskProgressService = Mock()
    AppProperties appProperties = new AppProperties()
    StepContribution stepContribution = Mock()

    OCRTasklet ocrTasklet = new OCRTasklet(
            documentStorage,
            languageDetector,
            documentTaskProgressService,
            appProperties)

    UUID documentId = UUID.randomUUID()
    Long databaseDocumentId = 42L

    def setup() {
        appProperties.tessdataPath = "tessdata"
        appProperties.maxParallelism = 2
        ReflectionTestUtils.setField(ocrTasklet, "documentId", documentId.toString())
        ReflectionTestUtils.setField(ocrTasklet, "spaceId", 1L)
    }

    def "should skip OCR and complete task when document has sufficient text"() {
        given:
        def pdfBytes = textPdfBytes("This page contains more than two hundred characters of extractable text. " * 4)
        def fsDocument = FsDocument.create(documentId, pdfBytes)
        def chunkContext = TaskletTestSupport.chunkContext(
                [(SetupTasklet.DATABASE_DOCUMENT_ID): databaseDocumentId])

        when:
        RepeatStatus status = ocrTasklet.execute(stepContribution, chunkContext)

        then:
        1 * documentStorage.read(documentId) >> fsDocument
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.OCR, 1)
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.OCR)
        0 * documentStorage.update(_)
        status == RepeatStatus.FINISHED
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
