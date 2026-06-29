package ch.supsi.documentapplication.integration

import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
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
class BaseFlowSpec extends Specification {

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

}
