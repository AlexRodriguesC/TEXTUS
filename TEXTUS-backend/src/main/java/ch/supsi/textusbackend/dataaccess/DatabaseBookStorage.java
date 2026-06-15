package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.model.Book;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
public class DatabaseBookStorage extends DatabaseStorage implements IDatabaseBookStorage {

    @Autowired
    public DatabaseBookStorage(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public Long create(Book book) {
        return jdbc.queryForObject("SELECT books.create_book(?)", Long.class, book.getOriginalFilename());
    }

    @Override
    public Book read(Long bookId) {
        return null;
    }

    @Override
    public void update(Book book) {}

    @Override
    public void delete(Long bookId) {}
}
