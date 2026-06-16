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

        Long indexId =
                jdbc.queryForObject("SELECT * FROM books.create_index(?, ?)", Long.class, bookId, bookIndex.page());

        for (int i = 0; i < indexEntries.size(); i++) {
            var entry = indexEntries.get(i);

            jdbc.queryForObject(
                    "SELECT books.create_index_entry(?, ?, ?, ?, ?)",
                    Long.class,
                    indexId,
                    entry.title(),
                    i,
                    entry.fromPage(),
                    entry.toPage());
        }
    }

    @Override
    public BookIndex read(Long bookId) {
        ch.supsi.textusbackend.model.BookIndex bookIndex = readIndexId(bookId);

        List<BookIndexEntry> entries = jdbc.query(
                "SELECT * FROM books.read_index_entries(?) ORDER BY position",
                (rs, rowNum) -> new BookIndexEntry(
                        rs.getString("title"), rs.getInt("position"), rs.getInt("from_page"), rs.getInt("to_page")),
                bookId);

        return new BookIndex(bookIndex.id(), bookIndex.page(), entries);
    }

    private BookIndex readIndexId(Long bookId) {
        return jdbc.queryForObject(
                "SELECT * FROM books.read_index(?)",
                (rs, numRow) -> new BookIndex(rs.getLong("id"), rs.getInt("page"), List.of()),
                bookId);
    }
}
