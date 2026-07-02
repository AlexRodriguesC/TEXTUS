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
        jdbcClient.sql("select * from documents.space where id = :id")
                .param("id", spaceId)
                .query()
                .listOfRows()
                .isEmpty()
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
}