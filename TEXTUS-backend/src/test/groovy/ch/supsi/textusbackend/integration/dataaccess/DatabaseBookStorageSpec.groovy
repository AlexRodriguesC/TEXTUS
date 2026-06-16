package ch.supsi.textusbackend.integration.dataaccess


import java.sql.ResultSet
import java.sql.Statement

class DatabaseBookStorageSpec extends DatabaseSpec {

    def "waits until postgres accepts jdbc connections"() {
        when:
        jdbcTemplate.execute("SELECT * FROM books.book")
        def abc = 3

        then:
        true
    }

}
