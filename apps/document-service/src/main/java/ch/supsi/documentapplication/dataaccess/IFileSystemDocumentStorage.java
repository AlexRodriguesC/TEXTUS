package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Chunk;
import ch.supsi.documentapplication.model.FsDocument;

import java.util.List;
import java.util.UUID;

public interface IFileSystemDocumentStorage {

    void create(FsDocument fsDocument);

    String createChunk(Integer page, String content, UUID documentId);

    void createChunkJSON(List<Chunk> chunks, UUID documentId);

    String readNextChunk(UUID documentId);

    void deleteChunk(UUID documentId, String chunkId);

    FsDocument read(UUID documentId);

    void update(FsDocument fsDocument);

    void delete(UUID documentId);

    void deleteAll();
}
