package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.model.Book;

/** A generic storage for books, exposing some of the CRUD operations needed */
public interface BookStorage {

    void create(Book book);

    Book read(Book.BookId bookId);

    void update(Book book);

    void delete(Book.BookId bookIdk);
}
