package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseTopicStorage
import ch.supsi.documentapplication.model.learn.Topic
import ch.supsi.documentapplication.service.rag.ChatRagFilterExpressions
import ch.supsi.documentapplication.service.rag.FallbackTopicDocumentPostProcessor
import org.springframework.ai.document.Document
import org.springframework.ai.embedding.Embedding
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.embedding.EmbeddingRequest
import org.springframework.ai.embedding.EmbeddingResponse
import org.springframework.ai.rag.Query
import org.springframework.ai.vectorstore.pgvector.PgVectorStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Subject

import java.nio.charset.StandardCharsets

class ChatRagVectorStoreSpec extends DatabaseSpec {

    @Subject
    DatabaseTopicStorage storage

    @Autowired
    JdbcTemplate jdbcTemplate

    PgVectorStore vectorStore

    def setup() {
        storage = new DatabaseTopicStorage(jdbcClient)
        EmbeddingModel embeddingModel = Stub(EmbeddingModel) {
            embed(_ as String) >> { String text -> vectorFor(text) }
            embed(_ as Document) >> { Document doc -> vectorFor(doc.getText()) }
            embed(_ as List, _, _) >> { List docs, opts, strategy -> docs.collect { textFrom(it) }.collect { vectorFor(it) } }
            embed(_ as List) >> { List docs -> docs.collect { textFrom(it) }.collect { vectorFor(it) } }
            call(_ as EmbeddingRequest) >> { EmbeddingRequest req ->
                def embeddings = req.instructions.collect { new Embedding(vectorFor(textFrom(it))) }
                new EmbeddingResponse(embeddings)
            }
        }
        vectorStore = PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(1536)
                .initializeSchema(true)
                .build()
        vectorStore.afterPropertiesSet()
    }

    def "vector rows store spaceId metadata for scoped retrieval"() {
        given:
        createUser()
        Long spaceA = createSpace()
        Long spaceB = createSpace()
        Long jobId = createBatchJobInstance()
        Long pageA = createPage(spaceA, jobId)
        Long pageB = createPage(spaceB, jobId)
        Long topicA = storage.create(new Topic(null, "a.pdf", 1, 1, "Space A Topic", "Alpha summary", []), pageA)
        Long topicB = storage.create(new Topic(null, "b.pdf", 1, 1, "Space B Topic", "Beta summary", []), pageB)

        vectorStore.add([
                topicDocument(topicA, pageA, spaceA, "Alpha summary"),
                topicDocument(topicB, pageB, spaceB, "Beta summary"),
        ])

        when:
        def spaceARows = jdbcClient
                .sql("""
                    SELECT count(*) AS total
                    FROM public.vector_store
                    WHERE metadata->>'spaceId' = :spaceId
                    """)
                .param("spaceId", spaceA.toString())
                .query()
                .singleValue()
        def spaceBRows = jdbcClient
                .sql("""
                    SELECT count(*) AS total
                    FROM public.vector_store
                    WHERE metadata->>'spaceId' = :spaceId
                    """)
                .param("spaceId", spaceB.toString())
                .query()
                .singleValue()

        then:
        spaceARows == 1
        spaceBRows == 1
        ChatRagFilterExpressions.spaceIdEquals(spaceA).toString().contains(spaceA.toString())
    }

    def "fallback processor suggests random topics when retrieval returns empty"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long jobId = createBatchJobInstance()
        Long pageId = createPage(spaceId, jobId)
        storage.create(new Topic(null, "notes.pdf", 1, 1, "Fallback Topic", "Fallback summary", []), pageId)
        def processor = new FallbackTopicDocumentPostProcessor(storage, spaceId, 3)
        def query = Query.builder().text("unrelated question").build()

        when:
        def fallbackDocs = processor.process(query, [])

        then:
        fallbackDocs.size() == 1
        fallbackDocs[0].metadata.get("fallback") == true
        fallbackDocs[0].getText().contains("Fallback Topic")
    }

    private static String textFrom(Object value) {
        value instanceof Document ? value.getText() : value.toString()
    }

    private static float[] vectorFor(String text) {
        def vector = new float[1536]
        def index = Math.floorMod(text.hashCode(), 1536)
        vector[index] = 1.0f
        vector
    }

    private static Document topicDocument(Long topicId, Long pageId, Long spaceId, String summary) {
        def documentId = UUID.nameUUIDFromBytes(("topic:" + topicId).getBytes(StandardCharsets.UTF_8)).toString()
        Document.builder()
                .id(documentId)
                .text(summary)
                .metadata([
                        topicId       : topicId,
                        pageId        : pageId,
                        spaceId       : spaceId,
                        embeddingModel: "text-embedding-3-small"
                ])
                .build()
    }
}
