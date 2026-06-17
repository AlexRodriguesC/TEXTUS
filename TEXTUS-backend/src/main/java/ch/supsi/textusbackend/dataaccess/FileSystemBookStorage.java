package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.exception.BookDomainException;
import ch.supsi.textusbackend.model.Book;
import ch.supsi.textusbackend.model.Chunk;
import ch.supsi.textusbackend.properties.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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
    public String createChunk(Integer page, String content, Long bookId) {
        String chunkId = String.valueOf(page);
        Path chunkDirectory = getChunkDirectoryPath(bookId);
        Path chunkPath = chunkDirectory.resolve(chunkId);

        try {
            Files.createDirectories(chunkDirectory);
            Files.writeString(chunkPath, content);
            return chunkId;
        } catch (Exception e) {
            String errorMessage = String.format("Unable to write chunk %s for book %s", chunkId, bookId);
            log.error(errorMessage, e);
            throw new BookDomainException(errorMessage);
        }
    }

    @Override
    public void createChunkJSON(List<Chunk> chunks, Long bookId) {
        try {
            Path outputFile = basePath.resolve("book_" + bookId + ".json");

            Files.createDirectories(outputFile.getParent());

            Map<String, Object> payload = Map.of(
                    "bookId", bookId,
                    "chunks", chunks);

            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(outputFile.toFile(), payload);

        } catch (Exception e) {
            String errorMessage = "Unable to create chunk JSON for book " + bookId;
            log.error(errorMessage, e);
            throw new BookDomainException(errorMessage);
        }
    }

    @Override
    public String readChunk(Long bookId, String chunkId) {
        Path chunkPath = getChunkDirectoryPath(bookId).resolve(chunkId);

        if (!Files.exists(chunkPath)) {
            return null;
        }

        try {
            return Files.readString(chunkPath);
        } catch (Exception e) {
            String errorMessage = String.format("Unable to read chunk %s for book %s", chunkId, bookId);
            log.error(errorMessage, e);
            throw new BookDomainException(errorMessage);
        }
    }

    @Override
    public String readNextChunk(Long bookId) {
        Path chunkDirectory = getChunkDirectoryPath(bookId);

        if (!Files.exists(chunkDirectory) || !Files.isDirectory(chunkDirectory)) {
            return null;
        }

        try (var stream = Files.list(chunkDirectory)) {
            Path nextChunkPath = stream.filter(Files::isRegularFile)
                    .min((p1, p2) -> {
                        Integer page1 = Integer.parseInt(p1.getFileName().toString());
                        Integer page2 = Integer.parseInt(p2.getFileName().toString());
                        return page1.compareTo(page2);
                    })
                    .orElse(null);

            if (nextChunkPath == null) {
                return null;
            }

            return Files.readString(nextChunkPath);
        } catch (Exception e) {
            String errorMessage = String.format("Unable to read next chunk for book %s", bookId);
            log.error(errorMessage, e);
            throw new BookDomainException(errorMessage);
        }
    }

    @Override
    public void deleteChunk(Long bookId, String chunkId) {
        Path chunkPath = getChunkDirectoryPath(bookId).resolve(chunkId);

        try {
            boolean deleted = Files.deleteIfExists(chunkPath);

            if (!deleted) {
                log.warn("Tried to delete chunk {} for book {} but it does not exist", chunkId, bookId);
            }

            Path chunkDirectory = getChunkDirectoryPath(bookId);
            if (Files.exists(chunkDirectory)
                    && Files.list(chunkDirectory).findAny().isEmpty()) {
                Files.deleteIfExists(chunkDirectory);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Unable to delete chunk %s for book %s", chunkId, bookId);
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
        Path chunkDirectory = getChunkDirectoryPath(bookId);

        boolean bookExists = Files.exists(bookPath);
        boolean chunkDirectoryExists = Files.exists(chunkDirectory);

        if (!bookExists && !chunkDirectoryExists) {
            log.warn("Tried to delete book with id {} but it does not exist", bookId);
            return;
        }

        try {
            if (bookExists) {
                Files.delete(bookPath);
            }

            if (chunkDirectoryExists) {
                try (var paths = Files.walk(chunkDirectory)) {
                    paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format("Unable to delete book %s", bookId);
            log.error(errorMessage, e);
            throw new BookDomainException(errorMessage);
        }
    }

    @Override
    public void deleteAll() {
        if (basePathDoesNotExist()) {
            log.warn("Tried to delete base path {} but it does not exist", basePath);
            return;
        }

        try (var paths = Files.walk(basePath)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            String errorMessage = String.format("Unable to delete base path %s", basePath);
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

    private Path getChunkDirectoryPath(Long bookId) {
        return basePath.resolve(bookId + "_chunks");
    }
}
