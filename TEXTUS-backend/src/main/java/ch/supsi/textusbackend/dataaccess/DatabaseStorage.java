package ch.supsi.textusbackend.dataaccess;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public abstract class DatabaseStorage {

    protected final JdbcTemplate jdbc;

    public DatabaseStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
}
