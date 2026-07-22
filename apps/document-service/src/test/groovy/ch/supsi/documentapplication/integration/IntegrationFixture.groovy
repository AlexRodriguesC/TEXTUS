package ch.supsi.documentapplication.integration

import ch.supsi.documentapplication.dataaccess.DatabasePathStorage
import ch.supsi.documentapplication.dataaccess.DatabaseSpaceStorage
import ch.supsi.documentapplication.dataaccess.DatabaseTopicStorage
import ch.supsi.documentapplication.dataaccess.DatabaseUserStorage
import ch.supsi.documentapplication.model.Space
import ch.supsi.documentapplication.model.User
import ch.supsi.documentapplication.model.learn.Chapter
import ch.supsi.documentapplication.model.learn.Lesson
import ch.supsi.documentapplication.model.learn.LessonType
import ch.supsi.documentapplication.model.learn.Topic
import org.springframework.batch.core.repository.dao.DefaultExecutionContextSerializer
import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.jdbc.core.simple.JdbcClient

trait IntegrationFixture {

    abstract JdbcClient getJdbcClient()

    Long createUser(String username = "owner", String password = "abc") {
        new DatabaseUserStorage(jdbcClient).createUser(new User(username, password))
        return 1L
    }

    Long createSpace(String owner = "owner", String icon = "icon", String name = "space", String description = "desc") {
        new DatabaseSpaceStorage(jdbcClient).create(new Space(owner, icon, name, description))
    }

    Long createBatchJobInstance(String jobName = "test-job") {
        Long jobInstanceId = jdbcClient
                .sql("SELECT COALESCE(MAX(job_instance_id), 0) + 1 FROM documents.batch_job_instance")
                .query(Long.class)
                .single()

        jdbcClient
                .sql("""
                INSERT INTO documents.batch_job_instance (job_instance_id, version, job_name, job_key)
                VALUES (:jobInstanceId, 0, :jobName, :jobKey)
                """)
                .param("jobInstanceId", jobInstanceId)
                .param("jobName", jobName)
                .param("jobKey", UUID.randomUUID().toString().substring(0, 32))
                .update()

        return jobInstanceId
    }

    void createBatchJobExecution(Long jobInstanceId, String status = "COMPLETED") {
        Long executionId = jdbcClient
                .sql("SELECT COALESCE(MAX(job_execution_id), 0) + 1 FROM documents.batch_job_execution")
                .query(Long.class)
                .single()

        jdbcClient
                .sql("""
                INSERT INTO documents.batch_job_execution
                (job_execution_id, version, job_instance_id, create_time, start_time, end_time, status, exit_code, last_updated)
                VALUES (:executionId, 0, :jobInstanceId, NOW(), NOW(), NOW(), :status, 'COMPLETED', NOW())
                """)
                .param("executionId", executionId)
                .param("jobInstanceId", jobInstanceId)
                .param("status", status)
                .update()

        def outputStream = new ByteArrayOutputStream()
        new DefaultExecutionContextSerializer().serialize([:] as Map<String, Object>, outputStream)
        String serializedContext = new String(outputStream.toByteArray(), "ISO-8859-1")

        jdbcClient
                .sql("""
                INSERT INTO documents.batch_job_execution_context (job_execution_id, short_context, serialized_context)
                VALUES (:executionId, :shortContext, :serializedContext)
                """)
                .param("executionId", executionId)
                .param("shortContext", "{}")
                .param("serializedContext", serializedContext)
                .update()
    }

    Long createDocument(Long spaceId, Long jobId, String name = "test-document") {
        jdbcClient
                .sql("""
                INSERT INTO documents.document (name, job_id, space_id)
                VALUES (:name, :jobId, :spaceId)
                RETURNING id
                """)
                .param("name", name)
                .param("jobId", jobId)
                .param("spaceId", spaceId)
                .query(Long.class)
                .single()
    }

    void createDocumentTask(
            Long documentId,
            String type,
            int progress = 0,
            int maxProgress = 1,
            String variant = "DEFAULT") {
        jdbcClient
                .sql("""
                INSERT INTO documents.document_task (id, document_id, type, progress, max_progress, variant)
                VALUES (:id, :documentId, :type, :progress, :maxProgress, :variant)
                """)
                .param("id", "${documentId}:${type}")
                .param("documentId", documentId)
                .param("type", type)
                .param("progress", progress)
                .param("maxProgress", maxProgress)
                .param("variant", variant)
                .update()
    }

    Long createPage(Long spaceId, Long jobId) {
        Long documentId = createDocument(spaceId, jobId)
        jdbcClient
                .sql("""
                INSERT INTO documents.page (space_id, document_id, title, summary)
                VALUES (:spaceId, :documentId, 'test title', 'test summary')
                RETURNING id
                """)
                .param("spaceId", spaceId)
                .param("documentId", documentId)
                .query(Long.class)
                .single()
    }

    Long createTopic(Long spaceId, Long jobId, String title = "Topic title") {
        Long pageId = createPage(spaceId, jobId)
        Topic topic = new Topic(null, "a.pdf", 1, title, "Topic summary", ["label1", "label2"])
        new DatabaseTopicStorage(jdbcClient).create(topic, pageId)
        jdbcClient
                .sql("SELECT id FROM documents.topic WHERE page_id = :pageId")
                .param("pageId", pageId)
                .query(Long.class)
                .single()
    }

    Map<String, Long> createLearningPathWithLesson(String owner = "owner") {
        createUser(owner, "abc")
        Long spaceId = createSpace(owner)
        Long jobId = createBatchJobInstance()
        DatabasePathStorage pathStorage = new DatabasePathStorage(jdbcClient)
        Long pathId = pathStorage.createLearningPath(spaceId, jobId)
        Long topicId = createTopic(spaceId, jobId)
        Lesson lesson = new Lesson(LessonType.LESSON, 0, [topicId])
        Chapter chapter = new Chapter(0, "Chapter 1", "desc", List.of())
        chapter.setLessons([lesson])
        pathStorage.create(pathId, chapter, spaceId)
        Long lessonId = jdbcClient
                .sql("""
                SELECT id FROM documents.lesson
                WHERE chapter_id = (SELECT id FROM documents.chapter WHERE path_id = :pathId)
                """)
                .param("pathId", pathId)
                .query(Long.class)
                .single()
        [spaceId: spaceId, pathId: pathId, lessonId: lessonId, topicId: topicId, jobId: jobId]
    }
}
