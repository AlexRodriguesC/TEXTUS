package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.model.BookIndex;
import ch.supsi.textusbackend.model.BookIndexEntry;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DatabaseIndexStorage extends DatabaseStorage implements IndexStorage {

    @Autowired
    public DatabaseIndexStorage(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public void create(BookIndex bookIndex, Long bookId) {
        var indexEntries = bookIndex.children();

        for (int i = 0; i < indexEntries.size(); i++) {
            var entry = indexEntries.get(i);

            jdbc.queryForObject(
                    "SELECT books.create_index(?, ?, ?, ?, ?)",
                    Long.class,
                    bookId,
                    entry.title(),
                    i,
                    entry.fromPage(),
                    entry.toPage());
        }
    }

    @Override
    public BookIndex read(Long bookId) {
        List<BookIndexEntry> entries = jdbc.query(
                "SELECT * FROM books.read_index(?) ORDER BY position",
                (rs, rowNum) -> new BookIndexEntry(
                        rs.getString("title"),
                        rs.getInt("position"),
                        rs.getInt("from_page"),
                        rs.getInt("to_page"),
                        List.of()),
                bookId);

        return new BookIndex(entries);
    }
}
