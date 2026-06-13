package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.model.Book;

public class DatabaseBookStorage implements BookStorage {

    @Override
    public void create(Book book) {}

    @Override
    public Book read(Book.BookId bookId) {
        return null;
    }

    @Override
    public void update(Book book) {}

    @Override
    public void delete(Book.BookId bookIdk) {}
}
