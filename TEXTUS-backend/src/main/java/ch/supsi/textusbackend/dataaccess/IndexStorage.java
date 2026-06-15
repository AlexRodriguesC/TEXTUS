package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.model.BookIndex;

public interface IndexStorage {

    void create(BookIndex bookIndex, Long bookId);

    BookIndex read(Long bookId);
}
