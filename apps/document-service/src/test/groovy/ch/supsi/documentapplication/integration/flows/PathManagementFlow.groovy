package ch.supsi.documentapplication.integration.flows

import ch.supsi.documentapplication.controller.DTO.request.CreateSpaceRequestDTO
import ch.supsi.documentapplication.controller.DTO.request.PatchSettingsDTO
import ch.supsi.documentapplication.controller.DTO.response.CreateSpaceResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.LearningPathResponseDTO
import ch.supsi.documentapplication.dataaccess.DatabasePathStorage
import ch.supsi.documentapplication.integration.BaseFlowSpec
import ch.supsi.documentapplication.integration.IntegrationFixture
import ch.supsi.documentapplication.model.learn.Chapter
import ch.supsi.documentapplication.model.learn.LearningPathStatus
import ch.supsi.documentapplication.model.learn.Lesson
import ch.supsi.documentapplication.model.learn.LessonType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

class PathManagementFlow extends BaseFlowSpec implements FlowTestSupport, IntegrationFixture {

    @Autowired
    JdbcClient jdbcClient

    ObjectMapper objectMapper = new ObjectMapper()

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    def "should get path update settings and delete path when learning path is seeded"() {
        given:
        String username = UUID.randomUUID().toString()
        String password = UUID.randomUUID().toString()
        def auth = registerAndLogin(username, password)
        Long spaceId = restTestClient
                .post()
                .uri('/api/v1/spaces')
                .contentType(MediaType.APPLICATION_JSON)
                .header('Authorization', authHeader(auth.token()))
                .body(new CreateSpaceRequestDTO("icon", "Path Space", "desc"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CreateSpaceResponseDTO)
                .returnResult()
                .responseBody
                .id()
        seedLearningPath(spaceId)

        when: "the user updates path settings"
        PatchSettingsDTO settings = objectMapper.readValue('''
        {
          "questionPercentage": 40,
          "listPercentage": 30,
          "groupPercentage": 20,
          "pairPercentage": 10
        }
        ''', PatchSettingsDTO)

        def settingsResponse = restTestClient
                .put()
                .uri("/api/v1/spaces/${spaceId}/paths/settings")
                .contentType(MediaType.APPLICATION_JSON)
                .header('Authorization', authHeader(auth.token()))
                .body(settings)
                .exchange()

        then:
        settingsResponse.expectStatus().isOk()

        when: "the user reads the learning path"
        def pathResponse = restTestClient
                .get()
                .uri("/api/v1/spaces/${spaceId}/paths")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        pathResponse.expectStatus().isOk()
        def pathBody = pathResponse.expectBody(LearningPathResponseDTO).returnResult().responseBody
        pathBody.settings().questionPercentage() == 40
        pathBody.status() in [LearningPathStatus.NOT_CREATED, LearningPathStatus.DONE, LearningPathStatus.GENERATING, LearningPathStatus.FAILED]

        when: "the user deletes the learning path"
        def deleteResponse = restTestClient
                .delete()
                .uri("/api/v1/spaces/${spaceId}/paths")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        deleteResponse.expectStatus().isNoContent()
    }

    private void seedLearningPath(Long spaceId) {
        Long jobId = createBatchJobInstance()
        createBatchJobExecution(jobId)
        DatabasePathStorage pathStorage = new DatabasePathStorage(jdbcClient)
        Long pathId = pathStorage.createLearningPath(spaceId, jobId)
        Long topicId = createTopic(spaceId, jobId)
        Lesson lesson = new Lesson(LessonType.LESSON, 0, [topicId])
        Chapter chapter = new Chapter(0, "Chapter 1", "desc", List.of())
        chapter.setLessons([lesson])
        pathStorage.create(pathId, chapter, spaceId)
    }
}
