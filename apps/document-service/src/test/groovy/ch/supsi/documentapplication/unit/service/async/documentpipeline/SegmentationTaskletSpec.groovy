package ch.supsi.documentapplication.unit.service.async.documentpipeline

import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage
import ch.supsi.documentapplication.model.FsDocument
import ch.supsi.documentapplication.model.TaskType
import ch.supsi.documentapplication.service.async.documentpipeline.SegmentationTasklet
import ch.supsi.documentapplication.service.async.documentpipeline.SetupTasklet
import ch.supsi.documentapplication.service.document.DocumentTaskProgressService
import ch.supsi.documentapplication.unit.service.async.TaskletTestSupport
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.springframework.batch.core.step.StepContribution
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

class SegmentationTaskletSpec extends Specification {

    IFileSystemDocumentStorage fileSystemDocumentStorage = Mock()
    DocumentTaskProgressService documentTaskProgressService = Mock()
    StepContribution stepContribution = Mock()
    SegmentationTasklet segmentationTasklet = new SegmentationTasklet(fileSystemDocumentStorage, documentTaskProgressService)

    UUID documentId = UUID.randomUUID()
    Long databaseDocumentId = 7L

    def setup() {
        ReflectionTestUtils.setField(segmentationTasklet, "documentId", documentId.toString())
        ReflectionTestUtils.setField(segmentationTasklet, "spaceId", 1L)
    }

    def "should extract page text and create chunk when document exists"() {
        given:
        def pdfBytes = sufficientTextPdfBytes()
        def fsDocument = FsDocument.create(documentId, pdfBytes)
        def chunkContext = TaskletTestSupport.chunkContext(
                [(SetupTasklet.DATABASE_DOCUMENT_ID): databaseDocumentId])

        when:
        RepeatStatus status = segmentationTasklet.execute(stepContribution, chunkContext)

        then:
        1 * fileSystemDocumentStorage.read(documentId) >> fsDocument
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.SEGMENTATION, 1)
        1 * fileSystemDocumentStorage.chunkExists(documentId, 1) >> false
        1 * fileSystemDocumentStorage.createChunk(1, _ as String, documentId) >> "page-1"
        1 * documentTaskProgressService.updateProgress(databaseDocumentId, TaskType.SEGMENTATION, 1)
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.SEGMENTATION)
        status == RepeatStatus.FINISHED
    }

    def "should create chunk for short-text page instead of skipping it"() {
        given:
        def pdfBytes = shortTextPdfBytes()
        def fsDocument = FsDocument.create(documentId, pdfBytes)
        def chunkContext = TaskletTestSupport.chunkContext(
                [(SetupTasklet.DATABASE_DOCUMENT_ID): databaseDocumentId])

        when:
        RepeatStatus status = segmentationTasklet.execute(stepContribution, chunkContext)

        then:
        1 * fileSystemDocumentStorage.read(documentId) >> fsDocument
        1 * documentTaskProgressService.startTask(databaseDocumentId, TaskType.SEGMENTATION, 1)
        1 * fileSystemDocumentStorage.chunkExists(documentId, 1) >> false
        1 * fileSystemDocumentStorage.createChunk(1, { it.trim() == "hi" }, documentId) >> "page-1"
        1 * documentTaskProgressService.updateProgress(databaseDocumentId, TaskType.SEGMENTATION, 1)
        1 * documentTaskProgressService.completeTask(databaseDocumentId, TaskType.SEGMENTATION)
        status == RepeatStatus.FINISHED
    }

    private static byte[] sufficientTextPdfBytes() {
        textPdfBytes("This page contains enough text for segmentation to keep it.")
    }

    private static byte[] shortTextPdfBytes() {
        textPdfBytes("hi")
    }

    private static byte[] textPdfBytes(String text) {
        def document = new PDDocument()
        document.addPage(new PDPage())
        def content = new PDPageContentStream(document, document.getPage(0))
        content.beginText()
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12)
        content.newLineAtOffset(100, 700)
        content.showText(text)
        content.endText()
        content.close()
        def output = new ByteArrayOutputStream()
        document.save(output)
        document.close()
        return output.toByteArray()
    }
}
