package ch.supsi.textusbackend.model;

import lombok.Getter;

@Getter
public class Book {

    private final Long bookId;
    private final String originalFilename;
    private final String contentType;
    private final long size;
    private final String storageLocation;
    private final byte[] bytes;

    private Book(
            Long bookId, String originalFilename, String contentType, long size, String storageLocation, byte[] bytes) {
        this.bookId = bookId;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.storageLocation = storageLocation;
        this.bytes = bytes;
    }

    public static Book create(Long bookId, String originalFilename, long size, byte[] bytes) {
        if (size <= 0) throw new IllegalArgumentException("Book cannot be empty");
        return new Book(bookId, originalFilename, null, size, null, bytes);
    }

    public static Book create(String originalFilename, long size, byte[] bytes) {
        return create(-1L, originalFilename, size, bytes);
    }
}
