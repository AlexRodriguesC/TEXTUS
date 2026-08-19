package ch.supsi.documentapplication.dataaccess;

import ch.supsi.documentapplication.model.User;
import java.util.Optional;

public interface IDatabaseUserStorage {

    User createUser(User user);

    Optional<User> readUser(String username);
}
