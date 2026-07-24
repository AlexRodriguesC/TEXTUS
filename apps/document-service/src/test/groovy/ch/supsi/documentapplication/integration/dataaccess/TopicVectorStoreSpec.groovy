package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseTopicStorage
import ch.supsi.documentapplication.model.learn.Topic
import org.springframework.ai.document.Document
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.embedding.EmbeddingRequest
import org.springframework.ai.embedding.EmbeddingResponse
import org.springframework.ai.embedding.Embedding
import org.springframework.ai.vectorstore.pgvector.PgVectorStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Subject

import java.nio.charset.StandardCharsets

class TopicVectorStoreSpec extends DatabaseSpec {

    @Subject
    DatabaseTopicStorage storage

    @Autowired
    JdbcTemplate jdbcTemplate

    PgVectorStore vectorStore

    def setup() {
        storage = new DatabaseTopicStorage(jdbcClient)
        EmbeddingModel embeddingModel = Stub(EmbeddingModel) {
            embed(_ as String) >> new float[1536]
            embed(_ as Document) >> new float[1536]
            embed(_ as List, _, _) >> { List docs, opts, strategy -> docs.collect { new float[1536] } }
            embed(_ as List) >> { List docs -> docs.collect { new float[1536] } }
            call(_ as EmbeddingRequest) >> { EmbeddingRequest req ->
                def embeddings = req.instructions.collect { new Embedding(new float[1536]) }
                new EmbeddingResponse(embeddings)
            }
        }
        vectorStore = PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(1536)
                .initializeSchema(true)
                .build()
        vectorStore.afterPropertiesSet()
    }

    def "should store topic summary embedding in vector_store with topic metadata"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long jobId = createBatchJobInstance()
        Long pageId = createPage(spaceId, jobId)
        def topic = new Topic(null, "notes.pdf", 1, "Topic A", "Summary for embedding", ["label-a"])

        when:
        Long topicId = storage.create(topic, pageId)
        def documentId = UUID.nameUUIDFromBytes(("topic:" + topicId).getBytes(StandardCharsets.UTF_8)).toString()
        def document = Document.builder()
                .id(documentId)
                .text(topic.summary)
                .metadata([
                        topicId       : topicId,
                        pageId        : pageId,
                        embeddingModel: "text-embedding-3-small"
                ])
                .build()
        vectorStore.add([document])

        then:
        def rows = jdbcClient
                .sql("""
                    SELECT content, metadata, embedding IS NOT NULL AS has_embedding
                    FROM public.vector_store
                    WHERE metadata->>'topicId' = :topicId
                    """)
                .param("topicId", topicId.toString())
                .query()
                .listOfRows()

        rows.size() == 1
        rows[0].content == "Summary for embedding"
        rows[0].has_embedding == true
        def metadata = rows[0].metadata.toString()
        metadata.contains(topicId.toString())
        metadata.contains(pageId.toString())
        metadata.contains("text-embedding-3-small")
    }
}
