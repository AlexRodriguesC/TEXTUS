package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.DatabaseSpaceStorage
import ch.supsi.documentapplication.dataaccess.DatabaseUserStorage
import ch.supsi.documentapplication.exception.InvalidInputException
import ch.supsi.documentapplication.exception.NotFoundException
import ch.supsi.documentapplication.model.Page
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.model.User
import org.springframework.dao.DataAccessException
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

    def "should create page"() {
        given: "an existing space and document"
        def user = databaseUserStorage.createUser(new User("user1", "pwd"))
        def spaceId = databaseSpaceStorage.create(
                new Space(user.getUsername(), "abc", "space1", "description1"))
        def documentId = storage.create(spaceId, jobId(), "abc")
        def page = new Page(null, 1, "title1", "summary1")

        when: "creating a page for that document"
        def pageId = storage.create(page, spaceId, documentId)

        then: "a valid id is returned"
        pageId != null

        and: "the page can be retrieved for that document"
        def pages = storage.getPages(documentId)
        pages.size() == 1
        pages[0].id == pageId
        pages[0].title == "title1"
        pages[0].summary == "summary1"
        pages[0].pageNumber == 1
    }

    def "should throw if space does not exist for page creation"() {
        given: "a non-existing space id and an existing document id"
        def user = databaseUserStorage.createUser(new User("user2", "pwd"))
        def spaceId = databaseSpaceStorage.create(
                new Space(user.username, "def", "space2", "description2"))
        def documentId = storage.create(spaceId, jobId(), "abc")
        def page = new Page(null, 1, "title2", "summary2")
        def nonExistingSpaceId = -1L

        when: "creating a page with a non-existing space id"
        storage.create(page, nonExistingSpaceId, documentId)

        then: "a NotFoundException is thrown"
        thrown(NotFoundException)
    }

    def "should throw if document does not exist"() {
        given: "an existing space and a non-existing document id"
        def user = databaseUserStorage.createUser(new User("user3", "pwd"))
        def spaceId = databaseSpaceStorage.create(
                new Space(user.username, "abc", "space3", "description3"))
        def page = new Page(null, 1, "title3", "summary3")
        def nonExistingDocumentId = -1L

        when: "creating a page with a non-existing document id"
        storage.create(page, spaceId, nonExistingDocumentId)

        then: "a NotFoundException is thrown"
        thrown(NotFoundException)
    }

    def "should throw if space does not exist for document creation"() {
        given: "a non-existing space id"
        def nonExistingSpaceId = -1L

        when: "creating a document for a non-existing space"
        storage.create(nonExistingSpaceId, jobId(), "abc")

        then: "a NotFoundException is thrown"
        thrown(NotFoundException)
    }

    def "should append and return documents in order"() {
        given: "an existing space with two documents"
        def user = databaseUserStorage.createUser(new User("user4", "pwd"))
        def spaceId = databaseSpaceStorage.create(
                new Space(user.username, "abc", "space4", "description4"))
        def jobId1 = jobId()
        def jobId2 = jobId()
        def documentId1 = storage.create(spaceId, jobId1, "abc")
        def documentId2 = storage.create(spaceId, jobId2, "def")

        when: "retrieving documents for that space"
        def documents = storage.getDocuments(spaceId)

        then: "both documents are returned in append order"
        documents.size() == 2
        documents*.id == [documentId1, documentId2]
        documents*.jobId == [jobId1, jobId2]
        documents*.name == ["abc", "def"]
        documents*.orderIndex == [0, 1]
    }

    def "should move document and normalize sibling order"() {
        given: "an existing space with three documents"
        def user = databaseUserStorage.createUser(new User("user-order", "pwd"))
        def spaceId = databaseSpaceStorage.create(
                new Space(user.username, "abc", "ordered-space", "description"))
        def firstId = storage.create(spaceId, jobId(), "first")
        def secondId = storage.create(spaceId, jobId(), "second")
        def thirdId = storage.create(spaceId, jobId(), "third")

        when: "the last document is moved between the first two"
        storage.move(spaceId, thirdId, 1)

        then: "all documents have contiguous zero-based indices"
        def documents = storage.getDocuments(spaceId)
        documents*.id == [firstId, thirdId, secondId]
        documents*.orderIndex == [0, 1, 2]
    }

    def "should translate database wrong-space move to not found"() {
        given:
        def user = databaseUserStorage.createUser(new User("move-wrong-space", "pwd"))
        def sourceSpaceId = databaseSpaceStorage.create(
                new Space(user.username, "abc", "source-space", "description"))
        def targetSpaceId = databaseSpaceStorage.create(
                new Space(user.username, "abc", "target-space", "description"))
        def documentId = storage.create(sourceSpaceId, jobId(), "document")

        when:
        storage.move(targetSpaceId, documentId, 0)

        then:
        thrown(NotFoundException)
    }

    def "should translate database out-of-range move to invalid input"() {
        given:
        def user = databaseUserStorage.createUser(new User("move-out-of-range", "pwd"))
        def spaceId = databaseSpaceStorage.create(
                new Space(user.username, "abc", "bounded-space", "description"))
        def documentId = storage.create(spaceId, jobId(), "document")

        when:
        storage.move(spaceId, documentId, 1)

        then:
        thrown(InvalidInputException)
    }

    def "should expose stable SQLSTATE for database move validation"() {
        given:
        def user = databaseUserStorage.createUser(new User("move-sqlstate-${expectedSqlState}", "pwd"))
        def spaceId = databaseSpaceStorage.create(
                new Space(user.username, "abc", "sqlstate-space-${expectedSqlState}", "description"))
        def documentId = storage.create(spaceId, jobId(), "document")

        when:
        jdbcClient
                .sql("SELECT documents.move_document(:space_id, :document_id, :order_index)")
                .param("space_id", spaceId)
                .param("document_id", requestedDocumentId(documentId, wrongSpace))
                .param("order_index", requestedOrderIndex(wrongSpace))
                .query()
                .singleValue()

        then:
        def exception = thrown(DataAccessException)
        sqlState(exception) == expectedSqlState

        where:
        wrongSpace || expectedSqlState
        true       || "P0002"
        false      || "22023"
    }

    def "should return empty list if space does not exist"() {
        given: "a non-existing space id"
        def nonExistingSpaceId = -1L

        when: "retrieving documents for that space"
        def documents = storage.getDocuments(nonExistingSpaceId)

        then: "an empty list is returned"
        documents.isEmpty()
    }

    def "should return pages for document"() {
        given: "an existing document with two pages"
        def user = databaseUserStorage.createUser(new User("user5", "pwd"))
        def spaceId = databaseSpaceStorage.create(
                new Space(user.username, "abc", "space5", "description5"))
        def documentId = storage.create(spaceId, jobId(), "abc")
        def page1 = new Page(null, 1, "title5a", "summary5a")
        def page2 = new Page(null, 1, "title5b", "summary5b")
        def pageId1 = storage.create(page1, spaceId, documentId)
        def pageId2 = storage.create(page2, spaceId, documentId)

        when: "retrieving pages for that document"
        def pages = storage.getPages(documentId)

        then: "both pages are returned"
        pages.size() == 2
        pages*.id as Set == [pageId1, pageId2] as Set
        pages*.title as Set == ["title5a", "title5b"] as Set
        pages*.pageNumber as Set == [1] as Set
    }

    def "should return empty list if document doesnt exist"() {
        given: "a non-existing document id"
        def nonExistingDocumentId = -1L

        when: "retrieving pages for that document"
        def pages = storage.getPages(nonExistingDocumentId)

        then: "an empty list is returned"
        pages.isEmpty()
    }

    private Long jobId() {
        Long jobInstanceId = System.nanoTime()
        String jobName = "test-job"
        String jobKey = UUID.randomUUID().toString().replace("-", "").substring(0, 32)

        jdbcClient
                .sql("""
                    INSERT INTO documents.batch_job_instance(job_instance_id, version, job_name, job_key)
                    VALUES (:job_instance_id, :version, :job_name, :job_key)
                """)
                .param("job_instance_id", jobInstanceId)
                .param("version", 0L)
                .param("job_name", jobName)
                .param("job_key", jobKey)
                .update()

        jobInstanceId
    }

    private static Long requestedDocumentId(Long documentId, boolean wrongSpace) {
        wrongSpace ? documentId + 1_000_000L : documentId
    }

    private static int requestedOrderIndex(boolean wrongSpace) {
        wrongSpace ? 0 : 1
    }

    private static String sqlState(Throwable throwable) {
        Throwable current = throwable
        while (current != null) {
            if (current instanceof java.sql.SQLException) {
                return ((java.sql.SQLException) current).getSQLState()
            }
            current = current.cause
        }
        null
    }
}