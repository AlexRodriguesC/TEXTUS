package ch.supsi.textusbackend.service.processingpipeline;

import ch.supsi.textusbackend.dataaccess.IFileSystemBookStorage;
import ch.supsi.textusbackend.dataaccess.IndexStorage;
import ch.supsi.textusbackend.model.Book;
import ch.supsi.textusbackend.model.BookIndex;
import ch.supsi.textusbackend.model.BookIndexEntry;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@StepScope
public class ChunkContentTasklet implements Tasklet {

    private final IFileSystemBookStorage fileSystemBookStorage;
    private final IndexStorage indexStorage;
    private final Long bookId;

    @Autowired
    public ChunkContentTasklet(
            IFileSystemBookStorage fileSystemBookStorage,
            IndexStorage indexStorage,
            @Value("#{jobParameters['BOOK_ID']}") Long bookId) {
        this.fileSystemBookStorage = fileSystemBookStorage;
        this.indexStorage = indexStorage;
        this.bookId = bookId;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws IOException {
        log.debug("Executing chunk content step for book with bookId {}", bookId);

        Book book = fileSystemBookStorage.read(bookId);
        BookIndex bookIndex = indexStorage.read(bookId);
        var offset = bookIndex.page();

        List<String> chunks = new ArrayList<>();

        try (PDDocument pdfDocument = Loader.loadPDF(book.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();

            for (BookIndexEntry entry : bookIndex.children()) {
                var from = entry.fromPage() + offset;
                var to = entry.toPage() + offset;
                stripper.setStartPage(from);
                stripper.setEndPage(to);

                String text = stripper.getText(pdfDocument);

                if (!text.isBlank()) {
                    chunks.add(text);
                } else {
                    log.warn(
                            "Empty text for entry '{}' pages {}-{}, skipping",
                            entry.title(),
                            entry.fromPage(),
                            entry.toPage());
                }
            }
        }

        chunkContext
                .getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("CHUNKS", (Serializable) chunks);

        return RepeatStatus.FINISHED;
    }
}
