package ch.supsi.documentapplication.unit.configuration

import ch.supsi.documentapplication.configuration.ChatRagConfiguration
import ch.supsi.documentapplication.configuration.properties.PromptProperties
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.document.Document
import org.springframework.ai.rag.Query
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer
import org.springframework.ai.template.st.StTemplateRenderer
import org.springframework.core.io.DefaultResourceLoader
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

    def "chatRagQueryAugmenter renders context and query placeholders from templates"() {
        given:
        def templateRenderer = StTemplateRenderer.builder()
                .startDelimiterToken('<' as char)
                .endDelimiterToken('>' as char)
                .build()
        def promptProperties = new PromptProperties()
        promptProperties.chatRagUser = "classpath:/prompts/chat/rag-user-v1.st"
        promptProperties.chatRagEmptyContext = "classpath:/prompts/chat/rag-empty-context-v1.st"
        def resourceLoader = new DefaultResourceLoader()
        QueryAugmenter augmenter = configuration.chatRagQueryAugmenter(
                templateRenderer, promptProperties, resourceLoader)
        def query = Query.builder().text("What is photosynthesis?").build()
        def documents = [Document.builder().text("Plants convert sunlight to energy.").build()]

        when:
        def augmented = augmenter.augment(query, documents)

        then:
        noExceptionThrown()
        augmented.text().contains("What is photosynthesis?")
        augmented.text().contains("Plants convert sunlight to energy.")
        !augmented.text().contains("<context>")
        !augmented.text().contains("question_answer_context")
    }

    def "chatRagQueryAugmenter returns original query when retrieved context is empty"() {
        given:
        def templateRenderer = StTemplateRenderer.builder()
                .startDelimiterToken('<' as char)
                .endDelimiterToken('>' as char)
                .build()
        def promptProperties = new PromptProperties()
        promptProperties.chatRagUser = "classpath:/prompts/chat/rag-user-v1.st"
        promptProperties.chatRagEmptyContext = "classpath:/prompts/chat/rag-empty-context-v1.st"
        def resourceLoader = new DefaultResourceLoader()
        QueryAugmenter augmenter = configuration.chatRagQueryAugmenter(
                templateRenderer, promptProperties, resourceLoader)
        def query = Query.builder().text("What is photosynthesis?").build()

        when:
        def augmented = augmenter.augment(query, [])

        then:
        augmented.text() == "What is photosynthesis?"
    }
}
