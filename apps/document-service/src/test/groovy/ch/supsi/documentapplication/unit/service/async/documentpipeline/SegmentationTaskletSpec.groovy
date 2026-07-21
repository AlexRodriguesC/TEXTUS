package ch.supsi.documentapplication.unit.service.async.documentpipeline

import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage
import ch.supsi.documentapplication.model.FsDocument
import ch.supsi.documentapplication.service.async.documentpipeline.SegmentationTasklet
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
    StepContribution stepContribution = Mock()
    SegmentationTasklet segmentationTasklet = new SegmentationTasklet(fileSystemDocumentStorage)

    UUID documentId = UUID.randomUUID()

    def setup() {
        ReflectionTestUtils.setField(segmentationTasklet, "documentId", documentId.toString())
        ReflectionTestUtils.setField(segmentationTasklet, "spaceId", 1L)
    }

    def "should extract page text and create chunk when document exists"() {
        given:
        def pdfBytes = minimalPdfBytes()
        def fsDocument = FsDocument.create(documentId, pdfBytes)
        def chunkContext = TaskletTestSupport.chunkContext()

        when:
        RepeatStatus status = segmentationTasklet.execute(stepContribution, chunkContext)

        then:
        1 * fileSystemDocumentStorage.read(documentId) >> fsDocument
        1 * fileSystemDocumentStorage.chunkExists(documentId, 1) >> false
        1 * fileSystemDocumentStorage.createChunk(1, _ as String, documentId) >> "page-1"
        status == RepeatStatus.FINISHED
    }

    private static byte[] minimalPdfBytes() {
        def document = new PDDocument()
        document.addPage(new PDPage())
        def content = new PDPageContentStream(document, document.getPage(0))
        content.beginText()
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12)
        content.newLineAtOffset(100, 700)
        content.showText("This page contains enough text for segmentation to keep it.")
        content.endText()
        content.close()
        def output = new ByteArrayOutputStream()
        document.save(output)
        document.close()
        return output.toByteArray()
    }
}
