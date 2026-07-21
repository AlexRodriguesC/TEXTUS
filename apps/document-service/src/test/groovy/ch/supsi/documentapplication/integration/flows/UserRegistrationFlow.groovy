package ch.supsi.documentapplication.integration.flows

import ch.supsi.documentapplication.controller.DTO.request.CreateSpaceRequestDTO
import ch.supsi.documentapplication.controller.DTO.response.CreateSpaceResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.TokenResponseDTO
import ch.supsi.documentapplication.integration.BaseFlowSpec
import ch.supsi.documentapplication.model.User
import org.springframework.http.MediaType

class UserRegistrationFlow extends BaseFlowSpec implements FlowTestSupport {

    def "should register a user, allow them to log in and then perform authenticated requests"() {
        given:
        String username = UUID.randomUUID().toString()
        String password = UUID.randomUUID().toString()
        def user = new User(username, password)

        when: "the user registers"
        def responseBody = registerAndLogin(username, password)

        then:
        responseBody
        responseBody.token() != null
        !responseBody.token().isEmpty()

        when: "the user logs in"
        def loginResponse = restTestClient
                .post()
                .uri('/api/v1/auth/login')
                .contentType(MediaType.APPLICATION_JSON)
                .body(user)
                .exchange()

        then:
        loginResponse.expectStatus().isOk()
        def loginResponseBody = loginResponse.expectBody(TokenResponseDTO).returnResult().responseBody
        loginResponseBody
        loginResponseBody.token() != null
        !loginResponseBody.token().isEmpty()

        when: "the user creates a space"
        def createSpaceResponse = restTestClient
                .post()
                .uri('/api/v1/spaces')
                .contentType(MediaType.APPLICATION_JSON)
                .header('Authorization', authHeader(responseBody.token()))
                .body(new CreateSpaceRequestDTO("icon", 'My Space', 'desc'))
                .exchange()

        then:
        createSpaceResponse.expectStatus().isOk()
        def createSpaceResponseBody = createSpaceResponse.expectBody(CreateSpaceResponseDTO).returnResult().responseBody
        createSpaceResponseBody != null
        createSpaceResponseBody.id() != null
    }

}
