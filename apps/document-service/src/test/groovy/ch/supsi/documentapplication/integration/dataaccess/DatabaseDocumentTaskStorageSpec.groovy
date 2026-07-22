package ch.supsi.documentapplication.integration.dataaccess

import ch.supsi.documentapplication.dataaccess.DatabaseDocumentStorage
import ch.supsi.documentapplication.dataaccess.DatabaseDocumentTaskStorage
import ch.supsi.documentapplication.dataaccess.DatabaseSpaceStorage
import ch.supsi.documentapplication.dataaccess.DatabaseUserStorage
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.model.TaskType
import ch.supsi.documentapplication.model.TaskVariant
import ch.supsi.documentapplication.model.User
import spock.lang.Subject

class DatabaseDocumentTaskStorageSpec extends DatabaseSpec {

    DatabaseSpaceStorage databaseSpaceStorage
    DatabaseUserStorage databaseUserStorage
    DatabaseDocumentStorage documentStorage

    @Subject
    DatabaseDocumentTaskStorage storage

    def setup() {
        storage = new DatabaseDocumentTaskStorage(jdbcClient)
        documentStorage = new DatabaseDocumentStorage(jdbcClient)
        databaseSpaceStorage = new DatabaseSpaceStorage(jdbcClient)
        databaseUserStorage = new DatabaseUserStorage(jdbcClient)
    }

    def "should upsert and read document task"() {
        given:
        Long documentId = createDocument()

        when:
        storage.upsert(documentId, TaskType.OCR, 10)

        then:
        def tasks = storage.getByDocumentId(documentId)
        tasks.size() == 1
        with(tasks[0]) {
            id == "${documentId}:OCR"
            it.documentId == documentId
            type == TaskType.OCR
            progress == 0
            maxProgress == 10
            variant == TaskVariant.DEFAULT
        }
    }

    def "should reset task on upsert retry"() {
        given:
        Long documentId = createDocument()
        storage.upsert(documentId, TaskType.OCR, 5)
        storage.updateProgress(documentId, TaskType.OCR, 3)
        storage.fail(documentId, TaskType.OCR)

        when:
        storage.upsert(documentId, TaskType.OCR, 8)

        then:
        with(storage.getByDocumentId(documentId)[0]) {
            progress == 0
            maxProgress == 8
            variant == TaskVariant.DEFAULT
        }
    }

    def "should update progress"() {
        given:
        Long documentId = createDocument()
        storage.upsert(documentId, TaskType.SEGMENTATION, 4)

        when:
        storage.updateProgress(documentId, TaskType.SEGMENTATION, 2)

        then:
        storage.getByDocumentId(documentId)[0].progress == 2
    }

    def "should complete task"() {
        given:
        Long documentId = createDocument()
        storage.upsert(documentId, TaskType.SUMMARIZATION, 6)
        storage.updateProgress(documentId, TaskType.SUMMARIZATION, 4)

        when:
        storage.complete(documentId, TaskType.SUMMARIZATION)

        then:
        with(storage.getByDocumentId(documentId)[0]) {
            progress == 6
            maxProgress == 6
            variant == TaskVariant.SUCCESS
        }
    }

    def "should fail task while keeping progress"() {
        given:
        Long documentId = createDocument()
        storage.upsert(documentId, TaskType.TOPIC, 3)
        storage.updateProgress(documentId, TaskType.TOPIC, 1)

        when:
        storage.fail(documentId, TaskType.TOPIC)

        then:
        with(storage.getByDocumentId(documentId)[0]) {
            progress == 1
            variant == TaskVariant.ERROR
        }
    }

    def "should return empty list for document without tasks"() {
        given:
        Long documentId = createDocument()

        expect:
        storage.getByDocumentId(documentId).isEmpty()
    }

    def "should return multiple tasks ordered by type"() {
        given:
        Long documentId = createDocument()
        storage.upsert(documentId, TaskType.TOPIC, 1)
        storage.upsert(documentId, TaskType.OCR, 2)

        when:
        def tasks = storage.getByDocumentId(documentId)

        then:
        tasks*.type == [TaskType.OCR, TaskType.TOPIC]
    }

    private Long createDocument() {
        def user = databaseUserStorage.createUser(new User("user", "pwd"))
        def spaceId = databaseSpaceStorage.create(new Space(user.username, "icon", "space", "desc"))
        documentStorage.create(spaceId, jobId(), "doc.pdf")
    }

    private Long jobId() {
        Long jobInstanceId = System.nanoTime()
        jdbcClient
                .sql("""
                    INSERT INTO documents.batch_job_instance(job_instance_id, version, job_name, job_key)
                    VALUES (:job_instance_id, 0, 'test-job', :job_key)
                """)
                .param("job_instance_id", jobInstanceId)
                .param("job_key", UUID.randomUUID().toString().replace("-", "").substring(0, 32))
                .update()
        jobInstanceId
    }
}
