package ch.supsi.documentapplication.unit.database

import spock.lang.Specification

class DocumentOrderMigrationSpec extends Specification {

    def "migration drops read documents before repeatable return shape changes"() {
        given:
        def migration = getClass()
                .getResource("/database/migration/V1785000008__add_document_order_index.sql")
                .text

        expect:
        migration.contains("DROP FUNCTION IF EXISTS documents.read_documents(BIGINT);")
    }
}
