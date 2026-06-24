package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.User;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class DatabaseUserStorage extends DatabaseStorage implements IDatabaseUserStorage {

    private final JdbcClient jdbcClient;

    public DatabaseUserStorage(JdbcTemplate jdbc, JdbcClient jdbcClient) {
        super(jdbc);
        this.jdbcClient = jdbcClient;
    }

    @Override
    public User createUser(User user) {
        jdbcClient
                .sql("SELECT users.create_user(:username, :password)")
                .param("username", user.getUsername())
                .param("password", user.getPassword())
                .query((rs, rowNum) -> null)
                .list();
        return user;
    }

    @Override
    public Optional<User> readUser(String username) {
        return jdbcClient
                .sql("SELECT * FROM users.read_user_by_username(:username)")
                .param("username", username)
                .query((rs, unused) -> new User(rs.getString("username"), rs.getString("password")))
                .optional();
    }
}
