package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.exception.BookDomainException;
import ch.supsi.textusbackend.model.Book;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * FileSystem-specific {@link BookStorage} implementation, handling direct interactions with a FileSystem for book storage
 */
@Service
public class FileSystemBookStorage implements BookStorage {

    private static final Logger logger = LogManager.getLogger(FileSystemBookStorage.class);
    private static final Path BASE_PATH = Path.of("books");

    @Override
    public void create(Book book) {
        if (basePathDoesNotExist()) {
            try {
                Files.createDirectory(BASE_PATH);
            } catch (Exception e) {
                String errorMessage = String.format("Unable to create directory %s", BASE_PATH);
                logger.error(errorMessage, e);
                throw new BookDomainException(errorMessage);
            }
        }

        String bookId = book.getBookId().toString();
        Path bookPath = getBookPath(bookId);

        try {
            Files.write(bookPath, book.getBytes());
        } catch (Exception e) {
            String errorMessage = String.format("Unable to write book %s", bookId);
            logger.error(errorMessage, e);
            throw new BookDomainException(errorMessage);
        }
    }

    @Override
    public Book read(Book.BookId bookId) {
        if (basePathDoesNotExist()) {
            return null;
        }

        Path bookPath = getBookPath(bookId.toString());
        File bookFile = bookPath.toFile();
        if (!bookFile.exists()) {
            return null;
        }

        byte[] bookBytes;
        try (InputStream in = new FileInputStream(bookFile)) {
            bookBytes = in.readAllBytes();
        } catch (Exception e) {
            String errorMessage = String.format("Unable to read book %s", bookId);
            logger.error(errorMessage, e);
            throw new BookDomainException(errorMessage);
        }

        return Book.create(bookId, bookFile.getName(), bookBytes.length, bookBytes);
    }

    @Override
    public void delete(Book.BookId bookId) {
        if (basePathDoesNotExist()) {
            logger.warn("Tried to delete book with id {} but it does not exist", bookId);
            return;
        }

        Path bookPath = getBookPath(bookId.toString());
        File bookFile = bookPath.toFile();
        if (!bookFile.exists()) {
            logger.warn("Tried to delete book with id {} but it does not exist\", book.getBookId()", bookId);
            return;
        }

        try {
            Files.delete(bookPath);
        } catch (Exception e) {
            String errorMessage = String.format("Unable to delete book %s", bookId);
            logger.error(errorMessage, e);
            throw new BookDomainException(errorMessage);
        }
    }

    private boolean basePathDoesNotExist() {
        return !Files.exists(BASE_PATH);
    }

    private Path getBookPath(String bookId) {
        return Path.of(BASE_PATH + File.separator + Path.of(bookId));
    }
}
