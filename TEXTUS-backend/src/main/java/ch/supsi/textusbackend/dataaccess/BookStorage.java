package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.model.Book;

import java.io.File;

/**
 * A generic storage for books, exposing some of the CRUD operations needed
 */
public interface BookStorage {

    void create(Book book);
    Book read(Book.BookId bookId);
    void delete(Book.BookId bookIdk);

}
