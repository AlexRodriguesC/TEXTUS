package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.DatabaseSpaceStorage
import ch.supsi.documentapplication.dataaccess.DatabaseUserStorage
import ch.supsi.documentapplication.exception.NotFoundException
import ch.supsi.documentapplication.model.Document
import ch.supsi.documentapplication.model.DocumentStatus
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.model.User
import spock.lang.Subject

class DatabaseDocumentStorageSpec extends DatabaseSpec {

    DatabaseSpaceStorage databaseSpaceStorage

    DatabaseUserStorage databaseUserStorage

    @Subject
    DatabaseDocumentStorage storage

    def setup() {
        storage = new DatabaseDocumentStorage(jdbcClient)
        databaseSpaceStorage = new DatabaseSpaceStorage(jdbcClient)
        databaseUserStorage = new DatabaseUserStorage(jdbcClient)
    }

    private void createUser(String username, String password = "abc") {
        databaseUserStorage.createUser(new User(username, password))
    }

    private Long createSpace(String owner, String icon = "abc", String name = "space", String description = "desc") {
        return databaseSpaceStorage.create(new Space(owner, icon, name, description))
    }

    private Long createDocument(Long spaceId) {
        return storage.create(spaceId)
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

    def "create document throws when space does not exist"() {
        when:
        storage.create(999L)

        then:
        def ex = thrown(NotFoundException)
        ex.message.contains("Space")
    }

}