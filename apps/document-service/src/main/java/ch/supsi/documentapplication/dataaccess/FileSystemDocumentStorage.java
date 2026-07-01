package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.exception.FileSystemException;
import ch.supsi.documentapplication.model.FsDocument;
import ch.supsi.documentapplication.properties.AppProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * FileSystem-specific {@link IFileSystemDocumentStorage} implementation, handling direct interactions with a
 * FileSystem for document storage
 */
@Repository
@Log4j2
public class FileSystemDocumentStorage implements IFileSystemDocumentStorage {

    private final Path basePath;

    @Autowired
    public FileSystemDocumentStorage(AppProperties appProperties) {
        this.basePath = Path.of(appProperties.getBaseFilesystemPath());
    }

    @Override
    public void create(FsDocument fsDocument) {
        if (basePathDoesNotExist()) {
            try {
                Files.createDirectory(basePath);
            } catch (Exception e) {
                String errorMessage = String.format("Unable to create directory %s", basePath);
                log.error(errorMessage, e);
                throw new FileSystemException(errorMessage);
            }
        }

        String documentId = fsDocument.getDocumentId().toString();
        Path documentPath = getDocumentPath(documentId);

        try {
            Files.write(documentPath, fsDocument.getBytes());
        } catch (Exception e) {
            String errorMessage = String.format("Unable to write document %s", documentId);
            log.error(errorMessage, e);
            throw new FileSystemException(errorMessage);
        }
    }

    @Override
    public String createChunk(Integer page, String content, UUID documentId) {
        String pageId = String.valueOf(page);
        Path pageDirectory = getPageDirectoryPath(documentId);
        Path pagePath = pageDirectory.resolve(pageId);

        try {
            Files.createDirectories(pageDirectory);
            Files.writeString(pagePath, content);
            return pageId;
        } catch (Exception e) {
            String errorMessage = String.format("Unable to write page %s for document %s", pageId, documentId);
            log.error(errorMessage, e);
            throw new FileSystemException(errorMessage);
        }
    }

    @Override
    public String readNextPage(UUID documentId) {
        Path pageDirectory = getPageDirectoryPath(documentId);

        if (!Files.exists(pageDirectory) || !Files.isDirectory(pageDirectory)) {
            return null;
        }

        try (var stream = Files.list(pageDirectory)) {
            Path nextPagePath = stream.filter(Files::isRegularFile)
                    .min((p1, p2) -> {
                        Integer page1 = Integer.parseInt(p1.getFileName().toString());
                        Integer page2 = Integer.parseInt(p2.getFileName().toString());
                        return page1.compareTo(page2);
                    })
                    .orElse(null);

            if (nextPagePath == null) {
                return null;
            }

            return Files.readString(nextPagePath);
        } catch (Exception e) {
            String errorMessage = String.format("Unable to read next page for document %s", documentId);
            log.error(errorMessage, e);
            throw new FileSystemException(errorMessage);
        }
    }

    @Override
    public void deleteChunk(UUID documentId, String pageId) {
        Path pagePath = getPageDirectoryPath(documentId).resolve(pageId);

        try {
            boolean deleted = Files.deleteIfExists(pagePath);

            if (!deleted) {
                log.warn("Tried to delete page {} for document {} but it does not exist", pageId, documentId);
            }

            Path pathDirectory = getPageDirectoryPath(documentId);
            try (var stream = Files.list(pathDirectory)) {
                boolean isEmpty = stream.findAny().isEmpty();

                if (Files.exists(pathDirectory) && isEmpty) {
                    Files.deleteIfExists(pathDirectory);
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format("Unable to delete page %s for document %s", pageId, documentId);
            log.error(errorMessage, e);
            throw new FileSystemException(errorMessage);
        }
    }

    @Override
    public FsDocument read(UUID documentId) {
        if (basePathDoesNotExist()) {
            return null;
        }

        Path documentPath = getDocumentPath(String.valueOf(documentId));
        File documentFile = documentPath.toFile();
        if (!documentFile.exists()) {
            return null;
        }

        byte[] documentBytes;
        try (InputStream in = new FileInputStream(documentFile)) {
            documentBytes = in.readAllBytes();
        } catch (Exception e) {
            String errorMessage = String.format("Unable to read document %s", documentId);
            log.error(errorMessage, e);
            throw new FileSystemException(errorMessage);
        }

        return FsDocument.create(documentId, documentBytes);
    }

    @Override
    public void update(FsDocument fsDocument) {
        delete(fsDocument.getDocumentId());
        create(fsDocument);
    }

    @Override
    public void delete(UUID documentId) {
        if (basePathDoesNotExist()) {
            log.warn("Tried to delete document with id {} but it does not exist", documentId);
            return;
        }

        Path documentPath = getDocumentPath(String.valueOf(documentId));
        Path pageDirectory = getPageDirectoryPath(documentId);

        boolean documentExists = Files.exists(documentPath);
        boolean pageDirectoryExists = Files.exists(pageDirectory);

        if (!documentExists && !pageDirectoryExists) {
            log.warn("Tried to delete document with id {} but it does not exist", documentId);
            return;
        }

        try {
            if (documentExists) {
                Files.delete(documentPath);
            }

            if (pageDirectoryExists) {
                try (var paths = Files.walk(pageDirectory)) {
                    paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            throw new FileSystemException(e.getMessage());
                        }
                    });
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format("Unable to delete document %s", documentId);
            log.error(errorMessage, e);
            throw new FileSystemException(errorMessage);
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
            throw new FileSystemException(errorMessage);
        }
    }

    private boolean basePathDoesNotExist() {
        return !Files.exists(basePath);
    }

    private Path getDocumentPath(String documentId) {
        return Path.of(basePath + File.separator + Path.of(documentId));
    }

    private Path getPageDirectoryPath(UUID documentId) {
        return basePath.resolve(documentId + "_pages");
    }
}
