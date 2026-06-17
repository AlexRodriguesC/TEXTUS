package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.model.Book;
import ch.supsi.textusbackend.model.Chunk;
import java.util.List;

public interface IFileSystemBookStorage {

    void create(Book book);

    String createChunk(Integer page, String content, Long bookId);

    void createChunkJSON(List<Chunk> chunks, Long bookId);

    String readChunk(Long bookId, String chunkId);

    String readNextChunk(Long bookId);

    void deleteChunk(Long bookId, String chunkId);

    Book read(Long bookId);

    void update(Book book);

    void delete(Long bookId);

    void deleteAll();
}
