package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.FsDocument;
import java.util.UUID;

public interface IFileSystemDocumentStorage {

    void create(FsDocument fsDocument);

    String createChunk(Integer page, String content, UUID documentId);

    String readNextPage(UUID documentId);

    void deleteChunk(UUID documentId, String chunkId);

    FsDocument read(UUID documentId);

    void update(FsDocument fsDocument);

    void delete(UUID documentId);

    void deleteAll();
}
