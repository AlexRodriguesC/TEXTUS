package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseDocumentStorage
import ch.supsi.documentapplication.exception.NotFoundException
import ch.supsi.documentapplication.model.Chunk
import ch.supsi.documentapplication.model.Document
import ch.supsi.documentapplication.model.DocumentStatus
import spock.lang.Subject

class DatabaseDocumentStorageSpec extends DatabaseSpec {

    @Subject
    DatabaseDocumentStorage storage

    def setup() {
        storage = new DatabaseDocumentStorage(jdbcTemplate, jdbcClient)
    }

    private void createUser(String username, String password = "abc") {
        jdbcClient.sql("SELECT users.create_user(:username, :password)").param("username", username).param("password", password).query().singleValue()
    }

    private Long createSpace(String owner, String name = "space", String description = "desc") {
        return jdbcClient.sql("SELECT documents.create_space(:owner, :name, :description)")
                .param("owner", owner)
                .param("name", name)
                .param("description", description)
                .query(Long.class)
                .single()
    }

    private Long createDocument(Long spaceId) {
        return jdbcClient.sql("SELECT documents.create_document(:space_id)")
                .param("space_id", spaceId)
                .query(Long.class)
                .single()
    }

    private static Chunk buildChunk(List<String> keywords = ["k1", "k2"], List<String> relatedKeys = ["r1"]) {
        Chunk chunk = new Chunk()
        chunk.setTokenCountInput(11)
        chunk.setTokenCountOutput(7)
        chunk.setTitle("title")
        chunk.setSummary("summary")
        chunk.setKey("key")
        chunk.setKeywords(keywords)
        chunk.setRelatedKeys(relatedKeys)
        return chunk
    }

    def "should create chunk"() {
        given:
        def owner = "alice"
        createUser(owner)
        Long spaceId = createSpace(owner)
        Long documentId = createDocument(spaceId)
        Chunk chunk = buildChunk()

        when:
        Long chunkId = storage.create(chunk, spaceId, documentId)

        then:
        chunkId != null
        def row = jdbcTemplate.queryForMap("select * from documents.chunk where id = ?", chunkId)
        row.space_id == spaceId
        row.document_id == documentId
        row.tokens_in == 11
        row.tokens_out == 7
        row.title == "title"
        row.summary == "summary"
        row.key == "key"
    }

    def "should persist chunk keywords and related keys as arrays"() {
        given:
        def owner = "alice"
        createUser(owner)
        Long spaceId = createSpace(owner)
        Long documentId = createDocument(spaceId)
        Chunk chunk = buildChunk(["k1", "k2"], ["r1", "r2"])

        when:
        Long chunkId = storage.create(chunk, spaceId, documentId)

        then:
        chunkId != null
        List<String> keywords = jdbcTemplate.queryForList(
                "select keyword from documents.keyword where chunk_id = ? order by id",
                String,
                chunkId
        )
        keywords == ["k1", "k2"]
    }

    def "should create document"() {
        given:
        def owner = "alice"
        createUser(owner)
        Long spaceId = createSpace(owner)

        when:
        Long documentId = storage.create(spaceId)

        then:
        documentId != null
        def row = jdbcTemplate.queryForMap("select * from documents.document where id = ?", documentId)
        row.space_id == spaceId
        row.status == "RECEIVED"
    }

    def "should update document status"() {
        given:
        def owner = "alice"
        createUser(owner)
        Long spaceId = createSpace(owner)
        Long documentId = createDocument(spaceId)

        when:
        storage.updateDocumentStatus(documentId, DocumentStatus.PROCESSING)

        then:
        def row = jdbcTemplate.queryForMap("select status from documents.document where id = ?", documentId)
        row.status == "PROCESSING"
    }

    def "should read documents"() {
        given:
        def owner = "alice"
        createUser(owner)
        Long spaceId = createSpace(owner)
        Long documentId = createDocument(spaceId)
        jdbcTemplate.execute("update documents.document set status = 'DONE' where id = ${documentId}")

        when:
        List<Document> documents = storage.getDocuments(spaceId)

        then:
        documents.size() == 1
        documents[0].id == documentId
        documents[0].status == DocumentStatus.DONE
    }

    def "should read chunk"() {
        given:
        def owner = "alice"
        createUser(owner)
        Long spaceId = createSpace(owner)
        Long documentId = createDocument(spaceId)
        Chunk chunk = buildChunk(["k1", "k2"], ["r1", "r2"])
        Long chunkId = storage.create(chunk, spaceId, documentId)

        when:
        Chunk read = storage.read(chunkId)

        then:
        read.id == chunkId
        read.documentId == documentId
        read.tokenCountInput == 11
        read.tokenCountOutput == 7
        read.title == "title"
        read.summary == "summary"
        read.keywords == ["k1", "k2"]
        read.relatedKeys == ["r1", "r2"]
    }

    def "should return empty lists for null arrays when reading chunk"() {
        given:
        def owner = "alice"
        createUser(owner)
        Long spaceId = createSpace(owner)
        Long documentId = createDocument(spaceId)
        Chunk chunk = buildChunk([], [])
        Long chunkId = storage.create(chunk, spaceId, documentId)

        when:
        Chunk read = storage.read(chunkId)

        then:
        read.keywords == []
        read.relatedKeys == []
    }

    def "create chunk throws when space does not exist"() {
        given:
        Long spaceId = 999L
        Long documentId = 1L
        Chunk chunk = new Chunk()
        chunk.setTokenCountInput(1)
        chunk.setTokenCountOutput(1)
        chunk.setTitle("t")
        chunk.setSummary("s")
        chunk.setKey("k")
        chunk.setKeywords(["a"])
        chunk.setRelatedKeys(["b"])

        when:
        storage.create(chunk, spaceId, documentId)

        then:
        def ex = thrown(NotFoundException)
        ex.message.contains("Space")
    }

    def "create chunk throws when document does not exist"() {
        given:
        def owner = "alice"
        createUser(owner)
        Long spaceId = createSpace(owner)
        Long documentId = 999L
        Chunk chunk = new Chunk()
        chunk.setTokenCountInput(1)
        chunk.setTokenCountOutput(1)
        chunk.setTitle("t")
        chunk.setSummary("s")
        chunk.setKey("k")
        chunk.setKeywords(["a"])
        chunk.setRelatedKeys(["b"])

        when:
        storage.create(chunk, spaceId, documentId)

        then:
        def ex = thrown(NotFoundException)
        ex.message.contains("document 999 not found")
    }

    def "create document throws when space does not exist"() {
        when:
        storage.create(999L)

        then:
        def ex = thrown(NotFoundException)
        ex.message.contains("Space")
    }

}