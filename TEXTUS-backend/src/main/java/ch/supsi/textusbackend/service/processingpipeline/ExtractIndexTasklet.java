package ch.supsi.textusbackend.service.processingpipeline;

import ch.supsi.textusbackend.dataaccess.BookStorage;
import ch.supsi.textusbackend.model.Book;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

@Log4j2
@Component
@StepScope
public class ExtractIndexTasklet implements Tasklet {

    private final Resource promptUserResource;
    private final ChatClient chatClient;
    private final BookStorage bookStorage;

    @Value("#{jobParameters['BOOK_ID']}") // Injected from job params
    private String bookId;

    @Autowired
    public ExtractIndexTasklet(
            ChatClient.Builder builder,
            BookStorage bookStorage,
            @Value("classpath:/prompts/index-extraction-user-v1.st") Resource promptUserResource,
            @Value("classpath:/prompts/index-extraction-system-v1.st") Resource promptSystemResource) {
        this.chatClient = builder.defaultSystem(promptSystemResource).build();
        this.bookStorage = bookStorage;
        this.promptUserResource = promptUserResource;
    }

    record BookIndexEntry(String title, int pageNumber, List<BookIndexEntry> children) {}

    record BookIndex(List<BookIndexEntry> entries) {}

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

            String userPromptText = promptUserResource.getContentAsString(StandardCharsets.UTF_8);
            UserMessage userMessage =
                    UserMessage.builder().text(userPromptText).media(pageImages).build();

            BookIndex bookIndex =
                    chatClient.prompt(new Prompt(userMessage)).call().entity(BookIndex.class);

            var abc = 123;
        }

        return RepeatStatus.FINISHED;
    }
}
