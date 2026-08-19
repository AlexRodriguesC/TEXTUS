package ch.supsi.documentapplication.integration.flows

import ch.supsi.documentapplication.controller.DTO.request.CreateSpaceRequestDTO
import ch.supsi.documentapplication.controller.DTO.response.CreateSpaceResponseDTO
import ch.supsi.documentapplication.controller.DTO.response.DocumentsResponseDTO
import ch.supsi.documentapplication.integration.BaseFlowSpec
import ch.supsi.documentapplication.integration.IntegrationFixture
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

class DocumentManagementFlow extends BaseFlowSpec implements FlowTestSupport, IntegrationFixture {

    @Autowired
    JdbcClient jdbcClient

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    def "should accept upload list documents and delete seeded document when owner is authenticated"() {
        given:
        String username = UUID.randomUUID().toString()
        String password = UUID.randomUUID().toString()
        def auth = registerAndLogin(username, password)
        Long spaceId = restTestClient
                .post()
                .uri('/api/v1/spaces')
                .contentType(MediaType.APPLICATION_JSON)
                .header('Authorization', authHeader(auth.token()))
                .body(new CreateSpaceRequestDTO("icon", "Doc Space", "desc"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CreateSpaceResponseDTO)
                .returnResult()
                .responseBody
                .id()

        when: "the user uploads a document"
        def uploadBuilder = new MultipartBodyBuilder()
        uploadBuilder.part("document", new ByteArrayResource(minimalPdfBytes()))
                .filename("flow-test.pdf")
                .contentType(MediaType.APPLICATION_PDF)

        def uploadResponse = restTestClient
                .post()
                .uri("/api/v1/spaces/${spaceId}/documents")
                .header('Authorization', authHeader(auth.token()))
                .body(uploadBuilder.build())
                .exchange()

        then:
        uploadResponse.expectStatus().isAccepted()

        when: "the user lists documents"
        def listResponse = restTestClient
                .get()
                .uri("/api/v1/spaces/${spaceId}/documents")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        listResponse.expectStatus().isOk()
        listResponse.expectBody(DocumentsResponseDTO)
                .value { DocumentsResponseDTO body ->
                    assert body.documents() != null
                    body.documents().every { it.tasks() != null }
                }

        when: "the user deletes a seeded document"
        Long jobId = createBatchJobInstance()
        createBatchJobExecution(jobId)
        Long documentId = createDocument(spaceId, jobId, "seeded.pdf")
        def deleteResponse = restTestClient
                .delete()
                .uri("/api/v1/spaces/${spaceId}/documents/${documentId}")
                .header('Authorization', authHeader(auth.token()))
                .exchange()

        then:
        deleteResponse.expectStatus().isNoContent()
    }

    private static byte[] minimalPdfBytes() {
        "%PDF-1.4\n1 0 obj<<>>endobj\ntrailer<<>>\n%%EOF".bytes
    }
}
