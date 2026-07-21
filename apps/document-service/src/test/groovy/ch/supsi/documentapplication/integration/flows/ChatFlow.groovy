package ch.supsi.documentapplication.integration.flows

import ch.supsi.documentapplication.controller.DTO.request.CreateChatRequestDTO
import ch.supsi.documentapplication.controller.DTO.request.CreateSpaceRequestDTO
import ch.supsi.documentapplication.controller.DTO.response.ChatResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.ChatsResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.CreateSpaceResponseDTO
import ch.supsi.documentapplication.integration.BaseFlowSpec
import org.springframework.http.MediaType

class ChatFlow extends BaseFlowSpec implements FlowTestSupport {

    def "should create list and delete chat when owner is authenticated"() {
        given:
        String username = UUID.randomUUID().toString()
        String password = UUID.randomUUID().toString()
        def auth = registerAndLogin(username, password)
        Long spaceId = restTestClient
                .post()
                .uri('/api/v1/spaces')
                .contentType(MediaType.APPLICATION_JSON)
                .header('Authorization', authHeader(auth.token()))
                .body(new CreateSpaceRequestDTO("icon", "Chat Space", "desc"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CreateSpaceResponseDTO)
                .returnResult()
                .responseBody
                .id()

        when: "the user creates a chat"
        def createResponse = restTestClient
                .post()
                .uri("/api/v1/spaces/${spaceId}/chats")
                .contentType(MediaType.APPLICATION_JSON)
                .header('Authorization', authHeader(auth.token()))
                .body(new CreateChatRequestDTO("Study session"))
                .exchange()

        then:
        createResponse.expectStatus().isCreated()
        UUID chatId = createResponse.expectBody(ChatResponseDTO).returnResult().responseBody.id()

        when: "the user lists chats"
        def listResponse = restTestClient
                .get()
                .uri("/api/v1/spaces/${spaceId}/chats")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        listResponse.expectStatus().isOk()
        def chats = listResponse.expectBody(ChatsResponseDTO).returnResult().responseBody.chats()
        chats*.id().contains(chatId)

        when: "the user deletes the chat"
        def deleteResponse = restTestClient
                .delete()
                .uri("/api/v1/spaces/${spaceId}/chats/${chatId}")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        deleteResponse.expectStatus().isNoContent()
    }
}
