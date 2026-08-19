package ch.supsi.documentapplication.integration.flows

import ch.supsi.documentapplication.controller.DTO.request.CreateSpaceRequestDTO
import ch.supsi.documentapplication.controller.DTO.request.UpdateSpaceRequestDTO
import ch.supsi.documentapplication.controller.DTO.response.CreateSpaceResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.ReadSpaceResponseDTO
import ch.supsi.documentapplication.integration.BaseFlowSpec
import org.springframework.http.MediaType

class SpaceManagementFlow extends BaseFlowSpec implements FlowTestSupport {

    def "should create read update and delete a space when owner is authenticated"() {
        given:
        String username = UUID.randomUUID().toString()
        String password = UUID.randomUUID().toString()
        def auth = registerAndLogin(username, password)

        when: "the user creates a space"
        def createResponse = restTestClient
                .post()
                .uri('/api/v1/spaces')
                .contentType(MediaType.APPLICATION_JSON)
                .header('Authorization', authHeader(auth.token()))
                .body(new CreateSpaceRequestDTO("icon", "Flow Space", "Initial description"))
                .exchange()

        then:
        createResponse.expectStatus().isOk()
        Long spaceId = createResponse.expectBody(CreateSpaceResponseDTO).returnResult().responseBody.id()

        when: "the user lists spaces"
        def listResponse = restTestClient
                .get()
                .uri('/api/v1/spaces')
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        listResponse.expectStatus().isOk()
        def spaces = listResponse.expectBody(ReadSpaceResponseDTO[]).returnResult().responseBody
        spaces*.spaceId().contains(spaceId)

        when: "the user updates the space"
        def updateResponse = restTestClient
                .patch()
                .uri("/api/v1/spaces/${spaceId}")
                .contentType(MediaType.APPLICATION_JSON)
                .header('Authorization', authHeader(auth.token()))
                .body(new UpdateSpaceRequestDTO("new-icon", "Updated Space", "Updated description"))
                .exchange()

        then:
        updateResponse.expectStatus().isNoContent()

        when: "the user deletes the space"
        def deleteResponse = restTestClient
                .delete()
                .uri("/api/v1/spaces/${spaceId}")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        deleteResponse.expectStatus().isNoContent()
    }
}
