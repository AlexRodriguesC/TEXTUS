package ch.supsi.textusbackend.dataaccess;

import ch.supsi.textusbackend.exception.BookDomainException;
import ch.supsi.textusbackend.model.Book;
import ch.supsi.textusbackend.model.Chunk;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
public class DatabaseDocumentStorage extends DatabaseStorage implements IDatabaseDocumentStorage {

    @Autowired
    public DatabaseDocumentStorage(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    public Long create(Book book) {
        return jdbc.queryForObject("SELECT books.create_book(?)", Long.class, book.getOriginalFilename());
    }

    @Override
    public Long create(Chunk chunk, Long bookId) {
        return jdbc.execute((ConnectionCallback<Long>) connection -> {
            try (var stmt = connection.prepareStatement("SELECT books.add_chunk(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

                stmt.setLong(1, bookId);
                stmt.setInt(2, chunk.getTokenCountInput());
                stmt.setInt(3, chunk.getTokenCountOutput());

                stmt.setInt(4, chunk.getPage());
                stmt.setString(5, chunk.getTitle());
                stmt.setString(6, chunk.getSummary());
                stmt.setString(7, chunk.getKey());
                stmt.setArray(8, buildArray(chunk.getQuestions(), connection));
                stmt.setArray(9, buildArray(chunk.getKeywords(), connection));
                stmt.setArray(10, buildArray(chunk.getRelatedKeys(), connection));

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
    public List<Chunk> readChunks(Long bookId) {
        String sql = "select * from books.read_chunks(?)";

        return jdbc.query(
                sql,
                (rs, rowNum) -> new Chunk(
                        rs.getInt("tokens_in"),
                        rs.getInt("tokens_out"),
                        rs.getInt("page"),
                        rs.getString("title"),
                        rs.getString("summary"),
                        toList(rs.getArray("keywords")),
                        List.of(),
                        toList(rs.getArray("questions")),
                        rs.getString("key"),
                        toList(rs.getArray("related_keys"))),
                bookId);
    }

    private List<String> toList(java.sql.Array sqlArray) throws SQLException {
        if (sqlArray == null) {
            return List.of();
        }

        String[] values = (String[]) sqlArray.getArray();
        return values == null ? List.of() : List.of(values);
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
