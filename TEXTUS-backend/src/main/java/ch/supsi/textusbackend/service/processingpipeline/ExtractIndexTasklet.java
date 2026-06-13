package ch.supsi.textusbackend.service.processingpipeline;

import ch.supsi.textusbackend.dataaccess.BookStorage;
import ch.supsi.textusbackend.model.Book;
import ch.supsi.textusbackend.model.BookIndex;
import ch.supsi.textusbackend.service.clients.index.IndexExtractionClient;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.content.Media;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

@Log4j2
@Component
@StepScope
public class ExtractIndexTasklet implements Tasklet {

    private final IndexExtractionClient indexExtractionClient;
    private final BookStorage bookStorage;

    @Value("#{jobParameters['BOOK_ID']}") // Injected from job params
    private String bookId;

    @Autowired
    public ExtractIndexTasklet(IndexExtractionClient indexExtractionClient, BookStorage bookStorage) {
        this.indexExtractionClient = indexExtractionClient;
        this.bookStorage = bookStorage;
    }

    @Override
    public @Nullable RepeatStatus execute(@NotNull StepContribution contribution, @NotNull ChunkContext chunkContext)
            throws Exception {
        log.debug("Executing index extraction step for book with bookId {}", bookId);

        Book book = bookStorage.read(Book.BookId.of(bookId));

        try (PDDocument pdfDocument = Loader.loadPDF(book.getBytes())) {
            int lastPage = Math.min(20, pdfDocument.getNumberOfPages());
            PDFRenderer renderer = new PDFRenderer(pdfDocument);

            List<Media> pageImages = new ArrayList<>();
            for (int i = 0; i < lastPage; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 150);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                image.flush();
                pageImages.add(new Media(MimeTypeUtils.IMAGE_PNG, new ByteArrayResource(baos.toByteArray())));
            }

            BookIndex bookIndex = indexExtractionClient.extractBookIndex(pageImages);

            var abc = 123;
        }

        return RepeatStatus.FINISHED;
    }
}
