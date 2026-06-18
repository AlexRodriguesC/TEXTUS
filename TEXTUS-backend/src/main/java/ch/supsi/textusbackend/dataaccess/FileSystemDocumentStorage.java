package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.exception.DocumentDomainException;
import ch.supsi.textusbackend.model.Chunk;
import ch.supsi.textusbackend.model.Document;
import ch.supsi.textusbackend.properties.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
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
    public void create(Document document) {
        if (basePathDoesNotExist()) {
            try {
                Files.createDirectory(basePath);
            } catch (Exception e) {
                String errorMessage = String.format("Unable to create directory %s", basePath);
                log.error(errorMessage, e);
                throw new DocumentDomainException(errorMessage);
            }
        }

        String documentId = document.getDocumentId().toString();
        Path documentPath = getDocumentPath(documentId);

        try {
            Files.write(documentPath, document.getBytes());
        } catch (Exception e) {
            String errorMessage = String.format("Unable to write document %s", documentId);
            log.error(errorMessage, e);
            throw new DocumentDomainException(errorMessage);
        }
    }

    @Override
    public String createChunk(Integer page, String content, UUID documentId) {
        String chunkId = String.valueOf(page);
        Path chunkDirectory = getChunkDirectoryPath(documentId);
        Path chunkPath = chunkDirectory.resolve(chunkId);

        try {
            Files.createDirectories(chunkDirectory);
            Files.writeString(chunkPath, content);
            return chunkId;
        } catch (Exception e) {
            String errorMessage = String.format("Unable to write chunk %s for document %s", chunkId, documentId);
            log.error(errorMessage, e);
            throw new DocumentDomainException(errorMessage);
        }
    }

    @Override
    public void createChunkJSON(List<Chunk> chunks, UUID documentId) {
        try {
            Path outputFile = basePath.resolve("document_" + documentId + ".json");

            Files.createDirectories(outputFile.getParent());

            Map<String, Object> payload = Map.of("chunks", chunks);

            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(outputFile.toFile(), payload);

        } catch (Exception e) {
            String errorMessage = "Unable to create chunk JSON for document " + documentId;
            log.error(errorMessage, e);
            throw new DocumentDomainException(errorMessage);
        }
    }

    @Override
    public String readNextChunk(UUID documentId) {
        Path chunkDirectory = getChunkDirectoryPath(documentId);

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
            String errorMessage = String.format("Unable to read next chunk for document %s", documentId);
            log.error(errorMessage, e);
            throw new DocumentDomainException(errorMessage);
        }
    }

    @Override
    public void deleteChunk(UUID documentId, String chunkId) {
        Path chunkPath = getChunkDirectoryPath(documentId).resolve(chunkId);

        try {
            boolean deleted = Files.deleteIfExists(chunkPath);

            if (!deleted) {
                log.warn("Tried to delete chunk {} for document {} but it does not exist", chunkId, documentId);
            }

            Path chunkDirectory = getChunkDirectoryPath(documentId);
            if (Files.exists(chunkDirectory)
                    && Files.list(chunkDirectory).findAny().isEmpty()) {
                Files.deleteIfExists(chunkDirectory);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Unable to delete chunk %s for document %s", chunkId, documentId);
            log.error(errorMessage, e);
            throw new DocumentDomainException(errorMessage);
        }
    }

    @Override
    public Document read(UUID documentId) {
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
            throw new DocumentDomainException(errorMessage);
        }

        return Document.create(documentId, documentBytes);
    }

    @Override
    public void update(Document document) {
        delete(document.getDocumentId());
        create(document);
    }

    @Override
    public void delete(UUID documentId) {
        if (basePathDoesNotExist()) {
            log.warn("Tried to delete document with id {} but it does not exist", documentId);
            return;
        }

        Path documentPath = getDocumentPath(String.valueOf(documentId));
        Path chunkDirectory = getChunkDirectoryPath(documentId);

        boolean documentExists = Files.exists(documentPath);
        boolean chunkDirectoryExists = Files.exists(chunkDirectory);

        if (!documentExists && !chunkDirectoryExists) {
            log.warn("Tried to delete document with id {} but it does not exist", documentId);
            return;
        }

        try {
            if (documentExists) {
                Files.delete(documentPath);
            }

            if (chunkDirectoryExists) {
                try (var paths = Files.walk(chunkDirectory)) {
                    paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (Exception e) {
                            // todo: ?????? substitute with actual exception that makes sense..
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format("Unable to delete document %s", documentId);
            log.error(errorMessage, e);
            throw new DocumentDomainException(errorMessage);
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
            throw new DocumentDomainException(errorMessage);
        }
    }

    private boolean basePathDoesNotExist() {
        return !Files.exists(basePath);
    }

    private Path getDocumentPath(String documentId) {
        return Path.of(basePath + File.separator + Path.of(documentId));
    }

    private Path getChunkDirectoryPath(Long documentId) {
        return basePath.resolve(documentId + "_chunks");
    }

    private Path getChunkDirectoryPath(UUID documentId) {
        return basePath.resolve(documentId + "_chunks");
    }
}
