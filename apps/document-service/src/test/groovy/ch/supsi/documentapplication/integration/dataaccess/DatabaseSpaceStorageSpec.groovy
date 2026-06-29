package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseSpaceStorage
import ch.supsi.documentapplication.exception.NotFoundException
import ch.supsi.documentapplication.model.MindMap
import ch.supsi.documentapplication.model.MindMapStatus
import ch.supsi.documentapplication.model.Space
import spock.lang.Subject

class DatabaseSpaceStorageSpec extends DatabaseSpec {

    @Subject
    DatabaseSpaceStorage storage

    def setup() {
        storage = new DatabaseSpaceStorage(jdbcClient)
    }

    private Long createUser(String username = "alice", String password = "abc") {
        jdbcClient.sql("SELECT users.create_user(:username, :password)")
                .param("username", username)
                .param("password", password)
                .query()
                .singleValue()
        return 1L
    }

    private Long createSpace(String owner = "alice", String name = "space", String description = "desc") {
        return storage.create(owner, name, description)
    }

    def "should create space"() {
        when:
        createUser()
        Long spaceId = createSpace()

        then:
        spaceId != null
        def row = jdbcTemplate.queryForMap("select * from documents.space where id = ?", spaceId)
        row.owner_id == "alice"
        row.name == "space"
        row.description == "desc"
    }

    def "should throw when user does not exist"() {
        when:
        storage.create("missing", "space", "desc")

        then:
        def ex = thrown(NotFoundException)
        ex.message.toLowerCase().contains("user")
    }

    def "should read space"() {
        given:
        createUser()
        Long spaceId = createSpace()

        when:
        Optional<Space> space = storage.read(spaceId)

        then:
        space.present
        space.get().id == spaceId
        space.get().owner == "alice"
        space.get().name == "space"
        space.get().description == "desc"
    }

    def "should return empty optional when space does not exist"() {
        when:
        Optional<Space> space = storage.read(999L)

        then:
        !space.present
    }

    def "should read all spaces for owner"() {
        given:
        createUser()
        createUser("bob")
        createSpace("alice", "s1", "d1")
        createSpace("alice", "s2", "d2")
        createSpace("bob", "s3", "d3")

        when:
        List<Space> spaces = storage.readAll("alice")

        then:
        spaces.size() == 2
        spaces*.owner == ["alice", "alice"]
    }

    def "should update space"() {
        given:
        createUser()
        Long spaceId = createSpace()

        when:
        storage.update(spaceId, "new-name", "new-desc")

        then:
        def row = jdbcTemplate.queryForMap("select name, description from documents.space where id = ?", spaceId)
        row.name == "new-name"
        row.description == "new-desc"
    }

    def "should delete space"() {
        given:
        createUser()
        Long spaceId = createSpace()

        when:
        storage.delete(spaceId)

        then:
        jdbcTemplate.queryForList("select * from documents.space where id = ?", spaceId).isEmpty()
    }

    def "should return null when reading mind map and none exists"() {
        given:
        createUser()
        Long spaceId = createSpace()

        when:
        var map = storage.readMindMap(spaceId)

        then:
        map == null
    }

    def "should update mind map status"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long documentId = jdbcClient.sql("SELECT documents.create_document(:space_id)")
                .param("space_id", spaceId)
                .query(Long.class)
                .single()
        Long chunkId = jdbcClient.sql("""
        SELECT documents.create_chunk(
            :space_id, :tokens_in, :tokens_out, :title, :summary, :key, :keywords, :related_keys, :document_id
        )
    """)
                .param("space_id", spaceId)
                .param("tokens_in", 1)
                .param("tokens_out", 1)
                .param("title", "t")
                .param("summary", "s")
                .param("key", "k")
                .param("keywords", null)
                .param("related_keys", null)
                .param("document_id", documentId)
                .query(Long.class)
                .single()

        storage.createMindMap(spaceId)

        when:
        storage.updateMindMapStatus(spaceId, MindMapStatus.GENERATING)

        then:
        noExceptionThrown()

        and:
        MindMap result = storage.readMindMap(spaceId)
        result.mindMapStatus == MindMapStatus.GENERATING
    }
}