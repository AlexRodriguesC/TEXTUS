package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseSpaceStorage
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

    private Long createSpace(String owner = "alice", String icon = "abc", String name = "space", String description = "desc") {
        return storage.create(new Space(owner, icon, name, description))
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
        jdbcClient.sql("""
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