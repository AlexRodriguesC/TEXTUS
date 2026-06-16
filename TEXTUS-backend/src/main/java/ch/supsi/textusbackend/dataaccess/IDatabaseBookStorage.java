package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.model.Book;
import ch.supsi.textusbackend.model.Chunk;

public interface IDatabaseBookStorage {

    Long create(Book book);

    Long create(Chunk chunk, Long bookId);

    Book read(Long bookId);

    void update(Book book);

    void delete(Long bookId);
}
