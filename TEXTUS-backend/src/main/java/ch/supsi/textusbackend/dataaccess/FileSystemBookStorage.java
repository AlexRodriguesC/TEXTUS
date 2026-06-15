package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.exception.BookDomainException;
import ch.supsi.textusbackend.model.Book;
import ch.supsi.textusbackend.properties.AppProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * FileSystem-specific {@link IFileSystemBookStorage} implementation, handling direct interactions with a
 * FileSystem for book storage
 */
@Repository
@Log4j2
public class FileSystemBookStorage implements IFileSystemBookStorage {

    private final Path basePath;

    @Autowired
    public FileSystemBookStorage(AppProperties appProperties) {
        this.basePath = Path.of(appProperties.getBaseFilesystemPath());
    }

    @Override
    public void create(Book book) {
        if (basePathDoesNotExist()) {
            try {
                Files.createDirectory(basePath);
            } catch (Exception e) {
                String errorMessage = String.format("Unable to create directory %s", basePath);
                log.error(errorMessage, e);
                throw new BookDomainException(errorMessage);
            }
        }

        String bookId = book.getBookId().toString();
        Path bookPath = getBookPath(bookId);

        try {
            Files.write(bookPath, book.getBytes());
        } catch (Exception e) {
            String errorMessage = String.format("Unable to write book %s", bookId);
            log.error(errorMessage, e);
            throw new BookDomainException(errorMessage);
        }
    }

    @Override
    public Book read(Long bookId) {
        if (basePathDoesNotExist()) {
            return null;
        }

        Path bookPath = getBookPath(String.valueOf(bookId));
        File bookFile = bookPath.toFile();
        if (!bookFile.exists()) {
            return null;
        }

        byte[] bookBytes;
        try (InputStream in = new FileInputStream(bookFile)) {
            bookBytes = in.readAllBytes();
        } catch (Exception e) {
            String errorMessage = String.format("Unable to read book %s", bookId);
            log.error(errorMessage, e);
            throw new BookDomainException(errorMessage);
        }

        return Book.create(bookId, bookFile.getName(), bookBytes.length, bookBytes);
    }

    @Override
    public void update(Book book) {
        delete(book.getBookId());
        create(book);
    }

    @Override
    public void delete(Long bookId) {
        if (basePathDoesNotExist()) {
            log.warn("Tried to delete book with id {} but it does not exist", bookId);
            return;
        }

        Path bookPath = getBookPath(String.valueOf(bookId));
        File bookFile = bookPath.toFile();
        if (!bookFile.exists()) {
            log.warn("Tried to delete book with id {} but it does not exist", bookId);
            return;
        }

        try {
            Files.delete(bookPath);
        } catch (Exception e) {
            String errorMessage = String.format("Unable to delete book %s", bookId);
            log.error(errorMessage, e);
            throw new BookDomainException(errorMessage);
        }
    }

    private boolean basePathDoesNotExist() {
        return !Files.exists(basePath);
    }

    private Path getBookPath(String bookId) {
        return Path.of(basePath + File.separator + Path.of(bookId));
    }
}
