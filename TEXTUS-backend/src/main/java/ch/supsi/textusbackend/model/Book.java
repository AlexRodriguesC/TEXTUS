package ch.supsi.textusbackend.model;

import ch.supsi.textusbackend.exception.BookDomainException;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class Book {

    public record BookId(UUID uuid) {
        @Override
        public @NotNull String toString() {
            return uuid.toString();
        }

        public static BookId of(String id) {
            Pattern UUID_REGEX =
                    Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
            if (!UUID_REGEX.matcher(id).matches()) {
                throw new BookDomainException("Book ID is invalid");
            }

            return new BookId(UUID.fromString(id));
        }
    }

    private final BookId bookId;
    private final String originalFilename;
    private final String contentType;
    private final long size;
    private final String storageLocation;
    private final byte[] bytes;

    private Book(
            BookId bookId,
            String originalFilename,
            String contentType,
            long size,
            String storageLocation,
            byte[] bytes) {
        this.bookId = bookId;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.storageLocation = storageLocation;
        this.bytes = bytes;
    }

    public static Book create(BookId bookId, String originalFilename, long size, byte[] bytes) {
        if (size <= 0) throw new IllegalArgumentException("Book cannot be empty");
        return new Book(bookId, originalFilename, null, size, null, bytes);
    }

    public static Book create(String originalFilename, long size, byte[] bytes) {
        BookId bookId = new BookId(UUID.randomUUID());
        return create(bookId, originalFilename, size, bytes);
    }
}
