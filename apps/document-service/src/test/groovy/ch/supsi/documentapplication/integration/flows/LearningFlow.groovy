package ch.supsi.documentapplication.integration.flows

import ch.supsi.documentapplication.controller.DTO.request.CreateSpaceRequestDTO
import ch.supsi.documentapplication.controller.DTO.response.CreateSpaceResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.LessonResponseDTO
import ch.supsi.documentapplication.dataaccess.DatabasePathStorage
import ch.supsi.documentapplication.integration.BaseFlowSpec
import ch.supsi.documentapplication.integration.IntegrationFixture
import ch.supsi.documentapplication.model.learn.Chapter
import ch.supsi.documentapplication.model.learn.Lesson
import ch.supsi.documentapplication.model.learn.LessonType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

class LearningFlow extends BaseFlowSpec implements FlowTestSupport, IntegrationFixture {

    @Autowired
    JdbcClient jdbcClient

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    def "should return seeded lesson when owner requests lesson by id"() {
        given:
        String username = UUID.randomUUID().toString()
        String password = UUID.randomUUID().toString()
        def auth = registerAndLogin(username, password)
        Long spaceId = restTestClient
                .post()
                .uri('/api/v1/spaces')
                .contentType(MediaType.APPLICATION_JSON)
                .header('Authorization', authHeader(auth.token()))
                .body(new CreateSpaceRequestDTO("icon", "Learning Space", "desc"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CreateSpaceResponseDTO)
                .returnResult()
                .responseBody
                .id()
        Long lessonId = seedLesson(spaceId)

        when: "the user requests the lesson"
        def response = restTestClient
                .get()
                .uri("/api/v1/spaces/${spaceId}/lessons/${lessonId}")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        response.expectStatus().isOk()
        def lesson = response.expectBody(LessonResponseDTO).returnResult().responseBody
        lesson.id() == lessonId
        lesson.index() == 0
    }

    private Long seedLesson(Long spaceId) {
        Long jobId = createBatchJobInstance()
        DatabasePathStorage pathStorage = new DatabasePathStorage(jdbcClient)
        Long pathId = pathStorage.createLearningPath(spaceId, jobId)
        Long topicId = createTopic(spaceId, jobId)
        Long documentId = jdbcClient
                .sql("""
                SELECT page.document_id
                FROM documents.topic topic
                JOIN documents.page page ON page.id = topic.page_id
                WHERE topic.id = :topicId
                """)
                .param("topicId", topicId)
                .query(Long.class)
                .single()
        Lesson lesson = new Lesson(LessonType.LESSON, 0, [topicId])
        Chapter chapter = new Chapter(0, "Chapter 1", "desc", List.of(), documentId)
        chapter.setLessons([lesson])
        pathStorage.create(pathId, chapter, spaceId)
        jdbcClient
                .sql("""
                SELECT id FROM documents.lesson
                WHERE chapter_id = (SELECT id FROM documents.chapter WHERE path_id = :pathId)
                """)
                .param("pathId", pathId)
                .query(Long.class)
                .single()
    }
}
