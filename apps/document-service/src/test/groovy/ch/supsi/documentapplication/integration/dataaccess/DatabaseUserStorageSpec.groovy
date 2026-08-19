package ch.supsi.documentapplication.integration.dataaccess


import ch.supsi.documentapplication.dataaccess.DatabaseUserStorage
import ch.supsi.documentapplication.model.User
import spock.lang.Subject

class DatabaseUserStorageSpec extends DatabaseSpec  {

    @Subject
    DatabaseUserStorage storage

    def setup() {
        storage = new DatabaseUserStorage(jdbcClient)
    }

    def "should create user and return it"() {
        given:
        User user = new User("alice", "secret")

        when:
        User result = storage.createUser(user)

        then:
        result.username == "alice"
        result.password == "secret"

        and:
        def row = jdbcClient.sql("select username, password from users.\"user\" where username = :username")
                .param("username", "alice")
                .query()
                .singleRow()
        row.username == "alice"
        row.password == "secret"
    }

    def "should read existing user"() {
        given:
        storage.createUser(new User("alice", "secret"))

        when:
        Optional<User> result = storage.readUser("alice")

        then:
        result.present
        result.get().username == "alice"
        result.get().password == "secret"
    }

    def "should return optional empty when user does not exist"() {
        when:
        Optional<User> result = storage.readUser("missing")

        then:
        result == Optional.empty()
    }
}
