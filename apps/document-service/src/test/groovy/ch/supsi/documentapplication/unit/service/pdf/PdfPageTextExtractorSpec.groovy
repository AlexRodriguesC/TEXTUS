package ch.supsi.documentapplication.unit.service.pdf

import ch.supsi.documentapplication.service.pdf.PdfPageTextExtractor
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import spock.lang.Specification

class PdfPageTextExtractorSpec extends Specification {

    def "should extract one-based physical page texts in order"() {
        given:
        def pdfBytes = multiPagePdfBytes(["First page body", "Second page body", ""])

        when:
        def pages = PdfPageTextExtractor.extractPageTexts(pdfBytes)
        def count = PdfPageTextExtractor.pageCount(pdfBytes)

        then:
        count == 3
        pages.size() == 3
        pages[0].contains("First page body")
        pages[1].contains("Second page body")
        pages[2].isBlank()
    }

    def "should extract from an input stream"() {
        given:
        def pdfBytes = multiPagePdfBytes(["Stream page"])

        when:
        def pages = PdfPageTextExtractor.extractPageTexts(new ByteArrayInputStream(pdfBytes))

        then:
        pages.size() == 1
        pages[0].contains("Stream page")
    }

    private static byte[] multiPagePdfBytes(List<String> pageTexts) {
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
        def output = new ByteArrayOutputStream()
        document.save(output)
        document.close()
        return output.toByteArray()
    }
}
