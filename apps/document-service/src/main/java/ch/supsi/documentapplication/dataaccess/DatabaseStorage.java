package ch.supsi.documentapplication.dataaccess;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public abstract class DatabaseStorage {

    protected final JdbcClient jdbcClient;

    public DatabaseStorage(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }
}
