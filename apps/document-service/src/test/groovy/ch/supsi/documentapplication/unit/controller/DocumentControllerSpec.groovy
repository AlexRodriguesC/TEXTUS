package ch.supsi.documentapplication.unit.controller

import ch.supsi.documentapplication.controller.DocumentController
import ch.supsi.documentapplication.controller.SpaceController
import ch.supsi.documentapplication.properties.AppProperties
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
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
        def controller = new DocumentController(appProps, Mock(IDocumentService))
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

}
