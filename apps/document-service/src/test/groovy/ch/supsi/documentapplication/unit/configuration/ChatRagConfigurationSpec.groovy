package ch.supsi.documentapplication.unit.configuration

import ch.supsi.documentapplication.configuration.ChatRagConfiguration
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer
import spock.lang.Specification

class ChatRagConfigurationSpec extends Specification {

    ChatModel chatModel = Mock()
    ChatRagConfiguration configuration = new ChatRagConfiguration()

    def "ragQueryChatClientBuilder creates a dedicated builder"() {
        when:
        ChatClient.Builder builder = configuration.ragQueryChatClientBuilder(chatModel)

        then:
        builder != null
        builder.build() != null
    }

    def "query transformers are wired to the low-temperature builder"() {
        given:
        ChatClient.Builder ragBuilder = configuration.ragQueryChatClientBuilder(chatModel)

        when:
        CompressionQueryTransformer compression = configuration.compressionQueryTransformer(ragBuilder)
        RewriteQueryTransformer rewrite = configuration.rewriteQueryTransformer(ragBuilder)

        then:
        compression != null
        rewrite != null
        compression.class.name.contains("CompressionQueryTransformer")
        rewrite.class.name.contains("RewriteQueryTransformer")
    }
}
