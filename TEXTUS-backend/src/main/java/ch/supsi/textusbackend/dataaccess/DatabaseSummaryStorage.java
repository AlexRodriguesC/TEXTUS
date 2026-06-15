package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.model.BookIndexEntrySummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DatabaseSummaryStorage extends DatabaseBookStorage implements SummaryStorage {

    @Autowired
    public DatabaseSummaryStorage(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public Long createSummary(BookIndexEntrySummary bookIndexEntrySummary, Long bookId, Long indexId) {
        return jdbc.queryForObject(
                "SELECT * FROM books.create_summary(?,?,?)",
                Long.class,
                bookId,
                indexId,
                bookIndexEntrySummary.content());
    }
}
