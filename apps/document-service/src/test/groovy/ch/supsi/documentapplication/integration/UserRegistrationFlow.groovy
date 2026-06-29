package ch.supsi.documentapplication.integration


import ch.supsi.documentapplication.controller.DTO.request.CreateSpaceRequestDTO
import ch.supsi.documentapplication.controller.DTO.response.CreateSpaceResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.TokenResponseDTO
import ch.supsi.documentapplication.model.User
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserRegistrationFlow extends Specification {

    @LocalServerPort
    int port

    RestTestClient restTestClient

    def setup() {
        restTestClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:${port}")
                .build()
    }

    @Shared
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("abc")
            .withUsername("appuser")
            .withPassword("appsecret")

    static {
        postgreSQLContainer.start()
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl)
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername)
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword)
        registry.add("spring.flyway.enabled", () -> "true")
    }

    def "should register a user, allow them to log in and then perform authenticated requests"() {
        given:
        String username = UUID.randomUUID().toString()
        String password = UUID.randomUUID().toString()
        def user = new User(username, password)

        when: "the user registers"
        def response = restTestClient
                .post()
                .uri('/api/v1/auth/register')
                .contentType(MediaType.APPLICATION_JSON)
                .body(user)
                .exchange()

        then:
        response.expectStatus().isOk()
        def responseBody = response.expectBody(TokenResponseDTO).returnResult().responseBody
        responseBody
        responseBody.token() != null
        !responseBody.token().isEmpty()

        when: "the user logs in"
        def loginResponse = restTestClient
                .post()
                .uri('/api/v1/auth/login')
                .contentType(MediaType.APPLICATION_JSON)
                .body(user)
                .exchange()

        then:
        loginResponse.expectStatus().isOk()
        def loginResponseBody = loginResponse.expectBody(TokenResponseDTO).returnResult().responseBody
        loginResponseBody
        loginResponseBody.token() != null
        !loginResponseBody.token().isEmpty()

        when: "the user creates a space"
        def createSpaceResponse = restTestClient
                .post()
                .uri('/api/v1/spaces')
                .contentType(MediaType.APPLICATION_JSON)
                .header('Authorization', "Bearer ${responseBody.token()}")
                .body(new CreateSpaceRequestDTO('My Space', 'desc'))
                .exchange()

        then:
        createSpaceResponse.expectStatus().isOk()
        def createSpaceResponseBody = createSpaceResponse.expectBody(CreateSpaceResponseDTO).returnResult().responseBody
        createSpaceResponseBody != null
        createSpaceResponseBody.id() != null
    }

}
