package ch.supsi.documentapplication.integration.flows

import ch.supsi.documentapplication.controller.DTO.request.CreateSpaceRequestDTO
import ch.supsi.documentapplication.controller.DTO.response.CreateSpaceResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.MindMapResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.MindMapsResponseDTO
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
    def "should list generating mind map, fetch by id, and delete by id"() {
        given:
        String username = UUID.randomUUID().toString()
        String password = UUID.randomUUID().toString()
        def auth = registerAndLogin(username, password)
        Long spaceId = createOwnedSpace(auth.token(), "Mind Map Space")
        Long mindMapId = seedMindMap(spaceId, "STARTED")

        when: "the user lists mind maps"
        def listResponse = restTestClient
                .get()
                .uri("/api/v1/spaces/${spaceId}/mind-maps")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        listResponse.expectStatus().isOk()
        def listed = listResponse.expectBody(MindMapsResponseDTO).returnResult().responseBody
        listed.mindMaps().size() == 1
        listed.mindMaps()[0].id() == mindMapId
        listed.mindMaps()[0].status() == MindMapStatus.GENERATING

        when: "the user fetches the mind map by id"
        def detailResponse = restTestClient
                .get()
                .uri("/api/v1/spaces/${spaceId}/mind-maps/${mindMapId}")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        detailResponse.expectStatus().isOk()
        def detail = detailResponse.expectBody(MindMapResponseDTO).returnResult().responseBody
        detail.id == mindMapId
        detail.status == MindMapStatus.GENERATING

        when: "the user deletes the mind map by id"
        def deleteResponse = restTestClient
                .delete()
                .uri("/api/v1/spaces/${spaceId}/mind-maps/${mindMapId}")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        deleteResponse.expectStatus().isNoContent()
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    def "should surface FAILED status on list and detail"() {
        given:
        String username = UUID.randomUUID().toString()
        String password = UUID.randomUUID().toString()
        def auth = registerAndLogin(username, password)
        Long spaceId = createOwnedSpace(auth.token(), "Failed Mind Map Space")
        Long mindMapId = seedMindMap(spaceId, "FAILED")

        when:
        def listResponse = restTestClient
                .get()
                .uri("/api/v1/spaces/${spaceId}/mind-maps")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        listResponse.expectStatus().isOk()
        listResponse.expectBody(MindMapsResponseDTO).returnResult().responseBody.mindMaps()[0].status() == MindMapStatus.FAILED

        when:
        def detailResponse = restTestClient
                .get()
                .uri("/api/v1/spaces/${spaceId}/mind-maps/${mindMapId}")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        detailResponse.expectStatus().isOk()
        detailResponse.expectBody(MindMapResponseDTO).returnResult().responseBody.status == MindMapStatus.FAILED
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    def "should return 404 when deleting another space mind map id"() {
        given:
        String username = UUID.randomUUID().toString()
        String password = UUID.randomUUID().toString()
        def auth = registerAndLogin(username, password)
        Long spaceA = createOwnedSpace(auth.token(), "Space A")
        Long spaceB = createOwnedSpace(auth.token(), "Space B")
        Long mindMapIdA = seedMindMap(spaceA, "STARTED")

        when:
        def deleteResponse = restTestClient
                .delete()
                .uri("/api/v1/spaces/${spaceB}/mind-maps/${mindMapIdA}")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        deleteResponse.expectStatus().isNotFound()

        when: "space A mind map is still present"
        def listResponse = restTestClient
                .get()
                .uri("/api/v1/spaces/${spaceA}/mind-maps")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        listResponse.expectStatus().isOk()
        listResponse.expectBody(MindMapsResponseDTO).returnResult().responseBody.mindMaps()[0].id() == mindMapIdA
    }

    private Long createOwnedSpace(String token, String name) {
        return restTestClient
                .post()
                .uri('/api/v1/spaces')
                .contentType(MediaType.APPLICATION_JSON)
                .header('Authorization', authHeader(token))
                .body(new CreateSpaceRequestDTO("icon", name, "desc"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CreateSpaceResponseDTO)
                .returnResult()
                .responseBody
                .id()
    }

    private Long seedMindMap(Long spaceId, String batchStatus) {
        Long jobId = createBatchJobInstance("mind_map_creation_job")
        createBatchJobExecution(jobId, batchStatus)
        DatabaseSpaceStorage spaceStorage = new DatabaseSpaceStorage(jdbcClient)
        return spaceStorage.createMindMap(spaceId, jobId)
    }
}
