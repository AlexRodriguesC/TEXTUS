package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.Chunk;
import ch.supsi.documentapplication.model.Document;
import java.util.List;
import java.util.UUID;

public interface IFileSystemDocumentStorage {

    void create(Document document);

    String createChunk(Integer page, String content, UUID documentId);

    void createChunkJSON(List<Chunk> chunks, UUID documentId);

    String readNextChunk(UUID documentId);

    void deleteChunk(UUID documentId, String chunkId);

    Document read(UUID documentId);

    void update(Document document);

    void delete(UUID documentId);

    void deleteAll();
}
