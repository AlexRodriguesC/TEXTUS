package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.User;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class DatabaseUserStorage extends DatabaseStorage implements IDatabaseUserStorage {

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    public DatabaseUserStorage(JdbcClient jdbcClient) {
        super(jdbcClient);
    }

    @Override
    public User createUser(User user) {
        jdbcClient
                .sql("SELECT users.create_user(:username, :password)")
                .param(USERNAME, user.getUsername())
                .param(PASSWORD, user.getPassword())
                .query()
                .singleValue();
        return user;
    }

    @Override
    public Optional<User> readUser(String username) {
        return jdbcClient
                .sql("SELECT * FROM users.read_user_by_username(:username)")
                .param(USERNAME, username)
                .query((rs, unused) -> new User(rs.getString(USERNAME), rs.getString(PASSWORD)))
                .optional();
    }
}
