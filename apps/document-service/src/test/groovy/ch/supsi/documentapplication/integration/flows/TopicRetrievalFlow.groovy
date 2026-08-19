package ch.supsi.documentapplication.integration.flows

import ch.supsi.documentapplication.controller.DTO.request.CreateSpaceRequestDTO
import ch.supsi.documentapplication.controller.DTO.response.CreateSpaceResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.TopicsResponseDTO
import ch.supsi.documentapplication.integration.BaseFlowSpec
import ch.supsi.documentapplication.integration.IntegrationFixture
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

class TopicRetrievalFlow extends BaseFlowSpec implements FlowTestSupport, IntegrationFixture {

    @Autowired
    JdbcClient jdbcClient

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    def "should return seeded topic when owner requests topics for space"() {
        given:
        String username = UUID.randomUUID().toString()
        String password = UUID.randomUUID().toString()
        def auth = registerAndLogin(username, password)
        Long spaceId = restTestClient
                .post()
                .uri('/api/v1/spaces')
                .contentType(MediaType.APPLICATION_JSON)
                .header('Authorization', authHeader(auth.token()))
                .body(new CreateSpaceRequestDTO("icon", "Topic Space", "desc"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CreateSpaceResponseDTO)
                .returnResult()
                .responseBody
                .id()
        Long jobId = createBatchJobInstance()
        createTopic(spaceId, jobId, "Seeded Topic")

        when: "the user requests topics"
        def response = restTestClient
                .get()
                .uri("/api/v1/spaces/${spaceId}/topics")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        response.expectStatus().isOk()
        def topics = response.expectBody(TopicsResponseDTO).returnResult().responseBody.topics()
        topics*.title().contains("Seeded Topic")
    }
}
