package ch.supsi.documentapplication.integration.flows

import ch.supsi.documentapplication.controller.DTO.request.CreateSpaceRequestDTO
import ch.supsi.documentapplication.controller.DTO.response.CreateSpaceResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.MindMapJobDTO
import ch.supsi.documentapplication.dataaccess.DatabaseSpaceStorage
import ch.supsi.documentapplication.integration.BaseFlowSpec
import ch.supsi.documentapplication.integration.IntegrationFixture
import ch.supsi.documentapplication.model.MindMapStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

class MindMapManagementFlow extends BaseFlowSpec implements FlowTestSupport, IntegrationFixture {

    @Autowired
    JdbcClient jdbcClient

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    def "should get mind map job status and delete mind map when map is seeded"() {
        given:
        String username = UUID.randomUUID().toString()
        String password = UUID.randomUUID().toString()
        def auth = registerAndLogin(username, password)
        Long spaceId = restTestClient
                .post()
                .uri('/api/v1/spaces')
                .contentType(MediaType.APPLICATION_JSON)
                .header('Authorization', authHeader(auth.token()))
                .body(new CreateSpaceRequestDTO("icon", "Mind Map Space", "desc"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CreateSpaceResponseDTO)
                .returnResult()
                .responseBody
                .id()
        seedGeneratingMindMap(spaceId)

        when: "the user requests mind map job status"
        def jobResponse = restTestClient
                .get()
                .uri("/api/v1/spaces/${spaceId}/mind-maps/jobs")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        jobResponse.expectStatus().isOk()
        jobResponse.expectBody(MindMapJobDTO).returnResult().responseBody.status() == MindMapStatus.GENERATING

        when: "the user deletes the mind map"
        def deleteResponse = restTestClient
                .delete()
                .uri("/api/v1/spaces/${spaceId}/mind-maps")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        deleteResponse.expectStatus().isNoContent()
    }

    private void seedGeneratingMindMap(Long spaceId) {
        Long jobId = createBatchJobInstance("mind_map_creation_job")
        createBatchJobExecution(jobId, "STARTED")
        DatabaseSpaceStorage spaceStorage = new DatabaseSpaceStorage(jdbcClient)
        spaceStorage.createMindMap(spaceId, jobId)
    }
}
