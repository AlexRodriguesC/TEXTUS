package ch.supsi.textusbackend.service.processingpipeline;

import ch.supsi.textusbackend.dataaccess.IFileSystemDocumentStorage;
import ch.supsi.textusbackend.exception.BookDomainException;
import ch.supsi.textusbackend.model.Book;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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

    private final IFileSystemDocumentStorage bookFileSystemStorage;

    @Autowired
    public SegmentationTasklet(IFileSystemDocumentStorage bookFileSystemStorage) {
        this.bookFileSystemStorage = bookFileSystemStorage;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws Exception {

        Book book = bookFileSystemStorage.read(documentId);
        if (book == null) {
            throw new BookDomainException("Book not found: " + documentId);
        }

        List<String> contents = extractBookContents(book);

        contents = contents.stream()
                .filter(s -> !s.isBlank())
                .filter(s -> s.length() >= MIN_PAGE_TEXT_LENGTH)
                .collect(Collectors.toCollection(ArrayList::new));

        for (int page = 0; page < contents.size(); page++) {
            String pageText = contents.get(page);
            Integer pageNumber = page + 1;
            String chunkId = bookFileSystemStorage.createChunk(pageNumber, pageText, documentId);
            log.info("Created chunk {} for book {}, page {}", chunkId, documentId, page);
        }

        return RepeatStatus.FINISHED;
    }

    private List<String> extractBookContents(Book book) throws IOException {
        ArrayList<String> contents = new ArrayList<>();

        try (PDDocument pdfDocument = Loader.loadPDF(book.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            int pageCount = pdfDocument.getNumberOfPages();

            for (int page = 1; page <= pageCount; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);

                String rawPageText = stripper.getText(pdfDocument);
                String pageText = normalizePageText(rawPageText);

                contents.add(pageText);
            }
        }
        return contents;
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
