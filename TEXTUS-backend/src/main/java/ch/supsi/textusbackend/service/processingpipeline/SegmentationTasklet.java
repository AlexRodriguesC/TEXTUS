package ch.supsi.textusbackend.service.processingpipeline;

import ch.supsi.textusbackend.dataaccess.IFileSystemBookStorage;
import ch.supsi.textusbackend.exception.BookDomainException;
import ch.supsi.textusbackend.model.Book;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
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
public class SegmentationTasklet extends ProcessingTasklet {

    private static final int MIN_PAGE_TEXT_LENGTH = 40;

    private final IFileSystemBookStorage bookFileSystemStorage;

    @Autowired
    public SegmentationTasklet(IFileSystemBookStorage bookFileSystemStorage) {
        this.bookFileSystemStorage = bookFileSystemStorage;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws Exception {

        Book book = bookFileSystemStorage.read(bookId);
        if (book == null) {
            throw new BookDomainException("Book not found: " + bookId);
        }

        try (PDDocument pdfDocument = Loader.loadPDF(book.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            int pageCount = pdfDocument.getNumberOfPages();

            for (int page = 1; page <= pageCount; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);

                String rawPageText = stripper.getText(pdfDocument);
                String pageText = normalizePageText(rawPageText);

                if (pageText.isBlank()) {
                    log.info("Skipping empty page {} for book {}", page, bookId);
                    continue;
                }

                if (pageText.length() < MIN_PAGE_TEXT_LENGTH) {
                    log.info("Skipping short page {} for book {} ({} chars)", page, bookId, pageText.length());
                    continue;
                }

                String chunkId = bookFileSystemStorage.createChunk(page, pageText, bookId);
                log.info("Created chunk {} for book {} page {}", chunkId, bookId, page);
            }
        }

        return RepeatStatus.FINISHED;
    }

    private String normalizePageText(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\r", "")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
