package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseSpaceStorage
import ch.supsi.documentapplication.model.Space
import spock.lang.Subject

class DatabaseSpaceStorageSpec extends DatabaseSpec {

    @Subject
    DatabaseSpaceStorage storage

    def setup() {
        storage = new DatabaseSpaceStorage(jdbcClient)
    }

    def "should create space and read it back in round trip"() {
        given:
        createUser("space-owner")
        def space = new Space("space-owner", "icon1", "My Space", "Space description")

        when:
        Long spaceId = storage.create(space)
        def readSpace = storage.read(spaceId)

        then:
        readSpace.isPresent()
        readSpace.get().id == spaceId
        readSpace.get().owner == "space-owner"
        readSpace.get().icon == "icon1"
        readSpace.get().name == "My Space"
        readSpace.get().description == "Space description"
    }

    def "should create space and list it when reading all spaces for owner"() {
        given:
        createUser("list-owner")
        def space = new Space("list-owner", "icon2", "Listed Space", "Listed description")

        when:
        Long spaceId = storage.create(space)
        def spaces = storage.readAll("list-owner")

        then:
        spaces*.id.contains(spaceId)
        spaces.find { it.id == spaceId }.name == "Listed Space"
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

    def "should return mind map with job id but no nodes when generation is in progress"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long jobId = createBatchJobInstance()

        when:
        storage.createMindMap(spaceId, jobId)
        var map = storage.readMindMap(spaceId)

        then:
        map != null
        map.id != null
        map.jobId == jobId
        map.root == null
    }

    def "should delete mind map"() {
        given:
        createUser()
        Long spaceId = createSpace()
        Long jobId = createBatchJobInstance()
        storage.createMindMap(spaceId, jobId)

        when:
        storage.deleteMindMap(spaceId)
        var map = storage.readMindMap(spaceId)

        then:
        map == null
    }
}