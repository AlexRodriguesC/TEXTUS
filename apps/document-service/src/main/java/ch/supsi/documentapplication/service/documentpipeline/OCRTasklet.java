package ch.supsi.documentapplication.service.documentpipeline;

import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage;
import ch.supsi.documentapplication.model.FsDocument;
import ch.supsi.documentapplication.properties.AppProperties;
import ch.supsi.documentapplication.service.ProcessingTasklet;
import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import lombok.extern.log4j.Log4j2;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@StepScope
public class OCRTasklet extends ProcessingTasklet {

    private final IFileSystemDocumentStorage documentStorage;
    private final LanguageDetector languageDetector;
    private final String tessdataPath;

    @Autowired
    public OCRTasklet(
            IFileSystemDocumentStorage documentStorage,
            LanguageDetector languageDetector,
            AppProperties appProperties) {
        this.documentStorage = documentStorage;
        this.languageDetector = languageDetector;
        this.tessdataPath = appProperties.getTessdataPath();
    }

    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws Exception {
        log.debug("Executing OCR step for document with documentId {}", documentId);

        FsDocument fsDocument = documentStorage.read(getDocumentIdAsUUID());

        try (PDDocument pdfDocument = Loader.loadPDF(fsDocument.getBytes())) {
            if (!requiresOcr(pdfDocument)) {
                log.debug("Document {} does not require OCR, skipping", documentId);
                return RepeatStatus.FINISHED;
            }

            String tesseractLanguageCode = detectLanguage(pdfDocument);
            byte[] ocrPdfBytes = performOcr(pdfDocument, tesseractLanguageCode);

            FsDocument newFsDocument = FsDocument.create(fsDocument.getDocumentId(), ocrPdfBytes);
            documentStorage.update(newFsDocument);
        }

        return RepeatStatus.FINISHED;
    }

    private boolean requiresOcr(PDDocument pdfDocument) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(1);
        stripper.setEndPage(Math.min(5, pdfDocument.getNumberOfPages()));
        String sampleText = stripper.getText(pdfDocument);
        return sampleText.trim().length() < 200;
    }

    private String detectLanguage(PDDocument pdfDocument) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(1);
        stripper.setEndPage(Math.min(5, pdfDocument.getNumberOfPages()));
        String sampleText = stripper.getText(pdfDocument);
        Language language = languageDetector.detectLanguageOf(sampleText);
        return toTesseractCode(language);
    }

    private byte[] performOcr(PDDocument pdfDocument, String tesseractLanguageCode) throws Exception {
        PDFRenderer renderer = new PDFRenderer(pdfDocument);

        int threads = Math.min(4, Runtime.getRuntime().availableProcessors());

        long ocrStart = System.currentTimeMillis();
        List<Future<File>> futures = new ArrayList<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            Semaphore semaphore = new Semaphore(threads);

            for (int i = 0; i < pdfDocument.getNumberOfPages(); i++) {
                final int idx = i;
                semaphore.acquire();
                futures.add(executor.submit(() -> ocrPage(idx, renderer, tesseractLanguageCode, semaphore)));
            }

            executor.shutdown();
            boolean terminated = executor.awaitTermination(1L, TimeUnit.HOURS);
            if (!terminated) {
                log.error("OCR executor did not terminate gracefully within timeout");
            }
        }

        long ocrDuration = System.currentTimeMillis() - ocrStart;
        log.info(
                "OCR completed in {}ms ({}s) ({} pages, {} threads)",
                ocrDuration,
                ocrDuration / 1000.0,
                pdfDocument.getNumberOfPages(),
                threads);

        List<File> pagePdfs = collectResults(futures);
        return mergePages(pagePdfs);
    }

    private File ocrPage(int idx, PDFRenderer renderer, String tesseractLanguageCode, Semaphore semaphore)
            throws Exception {
        try {
            BufferedImage image;
            synchronized (this) {
                image = renderer.renderImageWithDPI(idx, 300);
            }

            File tmpImage = File.createTempFile("page_" + idx, ".png");
            ImageIO.write(image, "png", tmpImage);
            image.flush();

            Tesseract tesseract = new Tesseract();
            tesseract.setLanguage(tesseractLanguageCode);
            tesseract.setDatapath(tessdataPath);

            File tmpPdf = File.createTempFile("ocr_page_" + idx, "");
            tesseract.createDocuments(
                    tmpImage.getAbsolutePath(), tmpPdf.getAbsolutePath(), List.of(ITesseract.RenderedFormat.PDF));
            tmpImage.delete();

            return new File(tmpPdf.getAbsolutePath() + ".pdf");
        } finally {
            semaphore.release();
        }
    }

    private List<File> collectResults(List<Future<File>> futures) {
        return futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException("Failed to retrieve OCR result for page", e);
                    }
                })
                .toList();
    }

    private byte[] mergePages(List<File> pages) {
        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        merger.setDestinationStream(out);

        for (File pagePdf : pages) {
            try {
                merger.addSource(pagePdf);
            } catch (IOException e) {
                log.warn("File {} not found while trying to merge PDFs", pagePdf.getName());
            }
        }

        try {
            merger.mergeDocuments(null);
        } catch (IOException e) {
            log.error("Failed to write the merged document");
        }

        return out.toByteArray();
    }

    // Subset of the supported tesseract languages. The full list can be found here:
    // https://github.com/tesseract-ocr/tessdata
    private String toTesseractCode(Language language) {
        return switch (language) {
            case ENGLISH -> "eng";
            case FRENCH -> "fra";
            case GERMAN -> "deu";
            case SPANISH -> "spa";
            case ITALIAN -> "ita";
            case PORTUGUESE -> "por";
            case DUTCH -> "nld";
            case RUSSIAN -> "rus";
            case CHINESE -> "chi_sim";
            case JAPANESE -> "jpn";
            case KOREAN -> "kor";
            case ARABIC -> "ara";
            default -> "eng"; // Fallback
        };
    }
}
