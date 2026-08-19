package ch.supsi.documentapplication.unit.controller

import ch.supsi.documentapplication.controller.DocumentController
import ch.supsi.documentapplication.configuration.properties.AppProperties
import ch.supsi.documentapplication.service.AuthService
import ch.supsi.documentapplication.service.ISpaceService
import ch.supsi.documentapplication.service.document.IDocumentService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.core.Authentication
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

import ch.supsi.documentapplication.controller.DTO.mapper.DocumentTaskMapper
import ch.supsi.documentapplication.controller.DTO.response.TaskDto
import ch.supsi.documentapplication.model.Document
import ch.supsi.documentapplication.model.DocumentTask
import ch.supsi.documentapplication.model.TaskType
import ch.supsi.documentapplication.model.TaskVariant

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(DocumentController)
@AutoConfigureMockMvc(addFilters = false)
class DocumentControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    AuthenticationEntryPoint authenticationEntryPoint = Mock()

    @SpringBean
    ISpaceService spaceService = Mock()

    @SpringBean
    IDocumentService documentService = Mock()

    @SpringBean
    AuthService authService = Mock()

    @SpringBean
    Authentication authentication = Mock() {
        getName() >> "pippo"
    }

    @SpringBean
    AppProperties appProperties = Mock() {
        getMaxDocumentSize() >> 500L * 1024 * 1024
    }

    @SpringBean
    DocumentTaskMapper documentTaskMapper = new DocumentTaskMapper()

    def "should return 202 for valid file upload"() {
        given:
        def file = new MockMultipartFile("document", "doc.pdf", MediaType.APPLICATION_PDF_VALUE, "content".bytes)

        when:
        def result = mockMvc.perform(
                multipart("/api/v1/spaces/1/documents").file(file).principal(authentication))

        then:
        result.andExpect(status().isAccepted())
        1 * documentService.createDocument(_, "pippo", 1L)
    }

    def "should return 400 when file is empty"() {
        given:
        def file = new MockMultipartFile("document", "empty.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[0])

        when:
        def result = mockMvc.perform(
                multipart("/api/v1/spaces/1/documents").file(file))

        then:
        result.andExpect(status().isBadRequest())
        0 * documentService.createDocument(_, _)
    }

    def "should return 400 when file exceeds max size"() {
        given:
        def maxSize = 500L * 1024 * 1024
        def appProps = Mock(AppProperties) { it.maxDocumentSize >> maxSize }
        def controller = new DocumentController(appProps, Mock(IDocumentService), Mock(DocumentTaskMapper))
        def file = Mock(MultipartFile) {
            isEmpty() >> false
            getSize() >> maxSize
        }

        when:
        def response = controller.createDocument(file, 1L, authentication)

        then:
        response.statusCode.value() == 400
        0 * documentService.createDocument(_, _)
    }

    def "should return documents for space"() {
        given:
        documentService.getDocuments("pippo", 1L) >> {
            def doc = new Document("doc.pdf", 1L, 2L, 3)
            doc.tasks = [new DocumentTask("1:OCR", 1L, TaskType.OCR, 1, 1, TaskVariant.SUCCESS)]
            [doc]
        }

        when:
        def result = mockMvc.perform(get("/api/v1/spaces/1/documents").principal(authentication))

        then:
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.documents[0].id').value(1))
        result.andExpect(jsonPath('$.documents[0].orderIndex').value(3))
        result.andExpect(jsonPath('$.documents[0].tasks[0].type').value("OCR"))
        result.andExpect(jsonPath('$.documents[0].tasks[0].variant').value("SUCCESS"))
    }

    def "should move document"() {
        when:
        def result = mockMvc.perform(
                patch("/api/v1/spaces/1/documents/42")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content('{"orderIndex":0}'))

        then:
        1 * documentService.moveDocument("pippo", 1L, 42L, 0)
        result.andExpect(status().isNoContent())
    }

    def "should reject missing or negative document order index"() {
        when:
        def result = mockMvc.perform(
                patch("/api/v1/spaces/1/documents/42")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

        then:
        result.andExpect(status().isBadRequest())
        0 * documentService.moveDocument(_, _, _, _)

        where:
        body << ['{}', '{"orderIndex":-1}']
    }

    def "should delete document"() {
        when:
        def result = mockMvc.perform(delete("/api/v1/spaces/1/documents/42").principal(authentication))

        then:
        1 * documentService.deleteDocument("pippo", 1L, 42L)
        result.andExpect(status().isNoContent())
    }

    def "should retry document processing"() {
        when:
        def result = mockMvc.perform(post("/api/v1/spaces/1/documents/42/retry").principal(authentication))

        then:
        1 * documentService.retryDocument("pippo", 1L, 42L)
        result.andExpect(status().isAccepted())
    }

}
