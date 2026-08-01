package ch.supsi.documentapplication.service.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Extracts raw text from each physical PDF page using PDFBox, matching the
 * one-page-at-a-time behaviour used by document segmentation.
 */
public final class PdfPageTextExtractor {

    private PdfPageTextExtractor() {}

    /**
     * @param pdfBytes PDF document bytes
     * @return page texts in one-based physical page order (index 0 = page 1)
     */
    public static List<String> extractPageTexts(byte[] pdfBytes) throws IOException {
        Objects.requireNonNull(pdfBytes, "pdfBytes");
        try (PDDocument pdfDocument = Loader.loadPDF(pdfBytes)) {
            return extractPageTexts(pdfDocument);
        }
    }

    /**
     * @param inputStream PDF document stream (fully consumed; caller closes if needed)
     * @return page texts in one-based physical page order (index 0 = page 1)
     */
    public static List<String> extractPageTexts(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        try (PDDocument pdfDocument = Loader.loadPDF(inputStream.readAllBytes())) {
            return extractPageTexts(pdfDocument);
        }
    }

    public static int pageCount(byte[] pdfBytes) throws IOException {
        Objects.requireNonNull(pdfBytes, "pdfBytes");
        try (PDDocument pdfDocument = Loader.loadPDF(pdfBytes)) {
            return pdfDocument.getNumberOfPages();
        }
    }

    private static List<String> extractPageTexts(PDDocument pdfDocument) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        int pageCount = pdfDocument.getNumberOfPages();
        List<String> contents = new ArrayList<>(pageCount);

        for (int page = 1; page <= pageCount; page++) {
            stripper.setStartPage(page);
            stripper.setEndPage(page);
            contents.add(stripper.getText(pdfDocument));
        }
        return contents;
    }
}
