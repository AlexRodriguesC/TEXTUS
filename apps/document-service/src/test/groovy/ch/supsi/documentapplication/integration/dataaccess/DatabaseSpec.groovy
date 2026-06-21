package ch.supsi.documentapplication.integration.dataaccess

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest
abstract class DatabaseSpec extends Specification {

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
        registry.add("spring.datasource.url",      postgreSQLContainer::getJdbcUrl)
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername)
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword)
        registry.add("spring.flyway.enabled",      () -> "true")
    }

    @Autowired
    JdbcTemplate jdbcTemplate
}