package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.FsDocument;
import ch.supsi.documentapplication.model.PageChunk;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IFileSystemDocumentStorage {

    void create(FsDocument fsDocument);

    String createChunk(Integer page, String content, UUID documentId);

    boolean chunkExists(UUID documentId, int pageNumber);

    List<PageChunk> listChunks(UUID documentId, int limit);

    Optional<PageChunk> readNextChunk(UUID documentId);

    String readNextPage(UUID documentId);

    void deleteChunk(UUID documentId, String chunkId);

    FsDocument read(UUID documentId);

    void update(FsDocument fsDocument);

    void delete(UUID documentId);
}
