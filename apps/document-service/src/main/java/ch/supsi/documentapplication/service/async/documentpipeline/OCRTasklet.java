package ch.supsi.documentapplication.service.async.documentpipeline;

import ch.supsi.documentapplication.configuration.properties.AppProperties;
import ch.supsi.documentapplication.dataaccess.IFileSystemDocumentStorage;
import ch.supsi.documentapplication.model.FsDocument;
import ch.supsi.documentapplication.model.PageChunk;
import ch.supsi.documentapplication.model.TaskType;
import ch.supsi.documentapplication.service.async.ProcessingTasklet;
import ch.supsi.documentapplication.service.document.DocumentTaskProgressService;
import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@StepScope
public class OCRTasklet extends ProcessingTasklet {

    public static final int MIN_PAGE_TEXT_LENGTH = 40;
    private static final String OCR_TASK_STARTED = "OCR_TASK_STARTED";
    private static final String OCR_PROGRESS = "OCR_PROGRESS";
    private static final String LAST_OCR_ATTEMPTED_PAGE = "LAST_OCR_ATTEMPTED_PAGE";

    private final IFileSystemDocumentStorage documentStorage;
    private final LanguageDetector languageDetector;
    private final DocumentTaskProgressService documentTaskProgressService;
    private final OcrPageProcessor ocrPageProcessor;
    private final AppProperties appProperties;

    @Autowired
    public OCRTasklet(
            IFileSystemDocumentStorage documentStorage,
            LanguageDetector languageDetector,
            DocumentTaskProgressService documentTaskProgressService,
            OcrPageProcessor ocrPageProcessor,
            AppProperties appProperties) {
        this.documentStorage = documentStorage;
        this.languageDetector = languageDetector;
        this.documentTaskProgressService = documentTaskProgressService;
        this.ocrPageProcessor = ocrPageProcessor;
        this.appProperties = appProperties;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws Exception {
        log.debug("Executing OCR step for document with documentId {}", documentId);

        Long databaseDocumentId = getDatabaseDocumentId(chunkContext);
        UUID fsDocumentId = getDocumentIdAsUUID();
        ExecutionContext stepExecutionContext =
                chunkContext.getStepContext().getStepExecution().getExecutionContext();

        int lastAttemptedPage = getValue(stepExecutionContext, LAST_OCR_ATTEMPTED_PAGE, 0, Integer.class);
        List<Integer> pagesNeedingOcr = listPagesNeedingOcr(fsDocumentId, lastAttemptedPage);
        initializeTaskIfNeeded(stepExecutionContext, databaseDocumentId, pagesNeedingOcr.size());

        if (pagesNeedingOcr.isEmpty()) {
            log.debug("Document {} does not require OCR, skipping", documentId);
            documentTaskProgressService.completeTask(databaseDocumentId, TaskType.OCR);
            return RepeatStatus.FINISHED;
        }

        FsDocument fsDocument = documentStorage.read(fsDocumentId);
        int maxChunksPerStep = appProperties.getMaxChunksPerStep();
        int maxParallelism = appProperties.getMaxParallelism();
        int batchLimit = Math.min(maxChunksPerStep, pagesNeedingOcr.size());
        List<Integer> batch = pagesNeedingOcr.subList(0, batchLimit);

        try (PDDocument pdfDocument = Loader.loadPDF(fsDocument.getBytes())) {
            PDFRenderer renderer = new PDFRenderer(pdfDocument);
            String tesseractLanguageCode = detectLanguage(fsDocumentId);
            AtomicInteger progress = new AtomicInteger(stepExecutionContext.getInt(OCR_PROGRESS));

            ocrBatchInParallel(
                    batch,
                    renderer,
                    tesseractLanguageCode,
                    fsDocumentId,
                    databaseDocumentId,
                    stepExecutionContext,
                    progress,
                    maxParallelism);

            log.info("OCR processed {} pages for document {}", batch.size(), documentId);
        }

        int lastProcessedPage = batch.stream().max(Integer::compareTo).orElse(lastAttemptedPage);
        stepExecutionContext.putInt(LAST_OCR_ATTEMPTED_PAGE, lastProcessedPage);

        List<Integer> remainingPages = listPagesNeedingOcr(fsDocumentId, lastProcessedPage);
        if (remainingPages.isEmpty()) {
            documentTaskProgressService.completeTask(databaseDocumentId, TaskType.OCR);
            return RepeatStatus.FINISHED;
        }

        return RepeatStatus.CONTINUABLE;
    }

    private void initializeTaskIfNeeded(
            ExecutionContext stepExecutionContext, Long databaseDocumentId, int pagesNeedingOcrCount) {
        if (stepExecutionContext.containsKey(OCR_TASK_STARTED)) {
            return;
        }

        int maxProgress = Math.max(1, pagesNeedingOcrCount);
        documentTaskProgressService.startTask(databaseDocumentId, TaskType.OCR, maxProgress);
        stepExecutionContext.put(OCR_TASK_STARTED, true);
        stepExecutionContext.putInt(OCR_PROGRESS, 0);
        stepExecutionContext.putInt(LAST_OCR_ATTEMPTED_PAGE, 0);

        if (pagesNeedingOcrCount == 0) {
            documentTaskProgressService.completeTask(databaseDocumentId, TaskType.OCR);
        }
    }

    private List<Integer> listPagesNeedingOcr(UUID fsDocumentId, int minPageExclusive) {
        return documentStorage.listChunks(fsDocumentId, Integer.MAX_VALUE).stream()
                .filter(chunk -> chunk.pageNumber() > minPageExclusive)
                .filter(chunk -> needsOcr(chunk.content()))
                .map(PageChunk::pageNumber)
                .sorted()
                .toList();
    }

    private String detectLanguage(UUID fsDocumentId) {
        String sampleText = documentStorage.listChunks(fsDocumentId, Integer.MAX_VALUE).stream()
                .map(PageChunk::content)
                .filter(content -> !needsOcr(content))
                .findFirst()
                .orElse("");

        if (sampleText.isBlank()) {
            return "eng";
        }

        Language language = languageDetector.detectLanguageOf(sampleText);
        return toTesseractCode(language);
    }

    private void ocrBatchInParallel(
            List<Integer> batch,
            PDFRenderer renderer,
            String tesseractLanguageCode,
            UUID fsDocumentId,
            Long databaseDocumentId,
            ExecutionContext stepExecutionContext,
            AtomicInteger progress,
            int maxParallelism)
            throws Exception {
        int poolSize = Math.min(maxParallelism, batch.size());

        try (ExecutorService executor = Executors.newFixedThreadPool(poolSize)) {
            List<Future<Void>> futures = new ArrayList<>();

            for (Integer pageNumber : batch) {
                futures.add(executor.submit(() -> {
                    ocrAndOverwriteChunk(pageNumber, renderer, tesseractLanguageCode, fsDocumentId);
                    int updatedProgress = progress.incrementAndGet();
                    stepExecutionContext.putInt(OCR_PROGRESS, updatedProgress);
                    documentTaskProgressService.updateProgress(databaseDocumentId, TaskType.OCR, updatedProgress);
                    return null;
                }));
            }

            for (Future<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parallel OCR interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Parallel OCR failed", e.getCause());
        }
    }

    private void ocrAndOverwriteChunk(
            int pageNumber, PDFRenderer renderer, String tesseractLanguageCode, UUID fsDocumentId) throws Exception {
        BufferedImage image;
        synchronized (this) {
            image = renderer.renderImageWithDPI(pageNumber - 1, 300);
        }

        try {
            String rawText = ocrPageProcessor.extractText(image, tesseractLanguageCode);
            String pageText = normalizePageText(rawText);
            documentStorage.createChunk(pageNumber, pageText, fsDocumentId);
            log.debug("OCR overwrote chunk for page {} of document {}", pageNumber, documentId);
        } finally {
            image.flush();
        }
    }

    public boolean needsOcr(String text) {
        if (text == null) {
            return true;
        }
        return text.trim().length() < MIN_PAGE_TEXT_LENGTH;
    }

    public String normalizePageText(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\r", "")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
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
            default -> "eng";
        };
    }
}
