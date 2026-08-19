package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseAiUsageStorage
import ch.supsi.documentapplication.model.AiUsage
import spock.lang.Subject

class DatabaseAiUsageStorageSpec extends DatabaseSpec {

    @Subject
    DatabaseAiUsageStorage storage

    def setup() {
        storage = new DatabaseAiUsageStorage(jdbcClient)
    }

    def "should persist and read back ai usage"() {
        given:
        def usage = new AiUsage(42, 17, "llmgateway", "anthropic/claude-haiku-4-5-free")

        when:
        storage.create("page", 99L, "summarize", usage)
        def row = jdbcClient
                .sql("""
                SELECT entity_type, entity_id, operation, tokens_in, tokens_out, provider, model
                FROM documents.ai_usage
                WHERE entity_type = :entityType AND entity_id = :entityId
                """)
                .param("entityType", "page")
                .param("entityId", 99L)
                .query()
                .singleRow()

        then:
        row["entity_type"] == "page"
        row["entity_id"] == 99L
        row["operation"] == "summarize"
        row["tokens_in"] == 42
        row["tokens_out"] == 17
        row["provider"] == "llmgateway"
        row["model"] == "anthropic/claude-haiku-4-5-free"
    }
}
