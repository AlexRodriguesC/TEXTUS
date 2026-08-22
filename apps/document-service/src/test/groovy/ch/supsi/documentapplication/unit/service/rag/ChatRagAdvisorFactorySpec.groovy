package ch.supsi.documentapplication.unit.service.rag

import ch.supsi.documentapplication.configuration.properties.ChatRagProperties
import ch.supsi.documentapplication.dataaccess.IDatabaseTopicStorage
import ch.supsi.documentapplication.service.rag.ChatRagAdvisorFactory
import org.springframework.ai.chat.client.advisor.api.Advisor
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer
import org.springframework.ai.vectorstore.VectorStore
import spock.lang.Specification

class ChatRagAdvisorFactorySpec extends Specification {

    VectorStore vectorStore = Mock()
    IDatabaseTopicStorage topicStorage = Mock()
    ChatRagProperties ragProperties = new ChatRagProperties(
            similarityThreshold: 0.6d, topK: 4, fallbackTopicLimit: 3)
    CompressionQueryTransformer compressionQueryTransformer = Mock()
    RewriteQueryTransformer rewriteQueryTransformer = Mock()
    QueryAugmenter queryAugmenter = Mock()

    ChatRagAdvisorFactory factory = new ChatRagAdvisorFactory(
            vectorStore,
            topicStorage,
            ragProperties,
            compressionQueryTransformer,
            rewriteQueryTransformer,
            queryAugmenter)

    def "createForSpace returns a retrieval augmentation advisor"() {
        when:
        Advisor advisor = factory.createForSpace(99L)

        then:
        advisor != null
        advisor.name == "RetrievalAugmentationAdvisor"
    }

    def "createForSpace with pinned topic ids returns a retrieval augmentation advisor"() {
        when:
        Advisor advisor = factory.createForSpace(99L, [12L, 45L])

        then:
        advisor != null
        advisor.name == "RetrievalAugmentationAdvisor"
    }
}
