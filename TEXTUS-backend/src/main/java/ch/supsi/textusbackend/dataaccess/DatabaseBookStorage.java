package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.exception.BookDomainException;
import ch.supsi.textusbackend.model.Book;
import ch.supsi.textusbackend.model.Chunk;
import java.sql.Array;
import java.sql.Connection;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
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
    public Long create(Chunk chunk, Long bookId) {
        return jdbc.execute((ConnectionCallback<Long>) connection -> {
            try (var stmt = connection.prepareStatement("SELECT books.add_chunk(?, ?, ?, ?, ?, ?, ?, ?)")) {

                stmt.setLong(1, bookId);
                stmt.setInt(2, chunk.page());
                stmt.setString(3, chunk.title());
                stmt.setString(4, chunk.summary());
                stmt.setString(5, chunk.key());
                stmt.setArray(6, buildArray(chunk.questions(), connection));
                stmt.setArray(7, buildArray(chunk.keywords(), connection));
                stmt.setArray(8, buildArray(chunk.relatedKeys(), connection));

                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                    throw new BookDomainException("books.add_chunk returned no id");
                }
            }
        });
    }

    @Override
    public Book read(Long bookId) {
        return null;
    }

    @Override
    public void update(Book book) {}

    @Override
    public void delete(Long bookId) {}

    private Array buildArray(List<String> list, Connection connection) {
        var arr = list == null || list.isEmpty() ? new String[0] : list.toArray(new String[0]);
        try {
            return connection.createArrayOf("TEXT", arr);
        } catch (Exception e) {
            log.error("Error when trying to build sql array", e);
            return null;
        }
    }
}
