package ch.supsi.documentapplication.unit.service


import ch.supsi.documentapplication.dataaccess.IDatabaseUserStorage
import ch.supsi.documentapplication.exception.UsernameTakenException
import ch.supsi.documentapplication.model.User
import ch.supsi.documentapplication.properties.AppProperties
import ch.supsi.documentapplication.service.AuthService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

class AuthServiceSpec extends Specification {

    private final AppProperties appProperties = Stub(AppProperties) {
        getSecretKey() >> "12345678901234567890123456789012"
    }
    private final IDatabaseUserStorage databaseUserStorage = Mock(IDatabaseUserStorage)
    private final PasswordEncoder passwordEncoder = Mock(PasswordEncoder)

    private final AuthService service = new AuthService(appProperties, databaseUserStorage, passwordEncoder)

    def "should register user and return token"() {
        given:
        String username = "alice"
        String plainPassword = "plain"
        String encodedPassword = "encoded"
        User user = new User(username, plainPassword)

        passwordEncoder.encode(plainPassword) >> encodedPassword

        when:
        String token = service.register(user)

        then:
        1 * databaseUserStorage.readUser(username) >> Optional.empty()
        1 * passwordEncoder.encode(plainPassword)
        1 * databaseUserStorage.createUser(user)
        token != null
        service.validateToken(token)
        service.extractUsername(token) == username
    }

    def "should throw when username is already taken"() {
        given:
        String username = "alice"
        String plainPassword = "plain"
        User user = new User(username, plainPassword)

        when:
        service.register(user)

        then:
        1 * databaseUserStorage.readUser(username) >> Optional.of(user)
        UsernameTakenException e = thrown(UsernameTakenException)
        e.message == "Username is already taken"
        0 * passwordEncoder._
        0 * databaseUserStorage.createUser(_)
    }

    def "should generate token that can be validated and parsed"() {
        given:
        String username = "alice"

        when:
        String token = service.generateToken(username)

        then:
        token != null
        service.validateToken(token)
        service.extractUsername(token) == username
    }

    def "should return false for invalid token"() {
        given:
        String invalidToken = "not-a-valid-token"

        expect:
        !service.validateToken(invalidToken)
    }

    def "should load user by username"() {
        given:
        String username = "alice"
        String encodedPassword = "encoded-password"
        User user = new User(username, encodedPassword)

        when:
        UserDetails userDetails = service.loadUserByUsername(username)

        then:
        1 * databaseUserStorage.readUser(username) >> Optional.of(user)
        userDetails.username == username
        userDetails.password == encodedPassword
    }

    def "should throw when loading unknown username"() {
        given:
        String username = "alice"

        when:
        service.loadUserByUsername(username)

        then:
        1 * databaseUserStorage.readUser(username) >> Optional.empty()
        UsernameNotFoundException e = thrown(UsernameNotFoundException)
        e.message == username
    }
}