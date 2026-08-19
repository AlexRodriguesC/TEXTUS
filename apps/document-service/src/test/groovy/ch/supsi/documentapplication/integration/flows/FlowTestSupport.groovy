package ch.supsi.documentapplication.integration.flows

import ch.supsi.documentapplication.controller.DTO.response.TokenResponseDTO
import ch.supsi.documentapplication.model.User
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.RestTestClient

trait FlowTestSupport {

    abstract RestTestClient getRestTestClient()

    TokenResponseDTO registerAndLogin(String username, String password) {
        def user = new User(username, password)
        restTestClient
                .post()
                .uri('/api/v1/auth/register')
                .contentType(MediaType.APPLICATION_JSON)
                .body(user)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponseDTO)
                .returnResult()
                .responseBody
    }

    String authHeader(String token) {
        "Bearer ${token}"
    }
}
