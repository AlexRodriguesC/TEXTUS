package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.model.Book;

public interface IDatabaseBookStorage {

    Long create(Book book);

    Book read(Long bookId);

    void update(Book book);

    void delete(Long bookId);
}
